/**
 * 
 */
package org.jvnet.jenkins.plugins.nodelabelparameter.wrapper;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.ParameterValue;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.ParametersAction;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.BuildWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterValue;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.jvnet.jenkins.plugins.nodelabelparameter.NextLabelCause;
import org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterDefinition;
import org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterValue;

/**
 * This BuildWrapper is not marked as extension, it gets added dynamically to a
 * build by the ParameterValue implementations.
 * 
 * @author domi
 * @see LabelParameterValue#createBuildWrapper(AbstractBuild)
 * @see NodeParameterValue#createBuildWrapper(AbstractBuild)
 */
public class TriggerNextBuildWrapper extends BuildWrapper {

	private static final Logger LOGGER = Logger.getLogger(TriggerNextBuildWrapper.class.getName());

	final private String triggerIfResult;
	final private NodeParameterDefinition nodeParameterDefinition;

	public TriggerNextBuildWrapper(NodeParameterDefinition nodeParameterDefinition) {
		this.nodeParameterDefinition = nodeParameterDefinition;
		this.triggerIfResult = nodeParameterDefinition.getTriggerIfResult();
	}

	/**
	 * @see hudson.tasks.BuildStep#getRequiredMonitorService()
	 */
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		final ParametersAction origParamsAction = build.getAction(ParametersAction.class);
		if (origParamsAction == null) {
			// nothing we have to do
			return new Environment() {
			};
		}

		if (build.getProject().isConcurrentBuild() && !nodeParameterDefinition.isTriggerConcurrentBuilds()) {
			final String msg = Messages.BuildWrapper_param_not_concurrent(nodeParameterDefinition.getName());
			LOGGER.severe(msg);
			throw new IllegalStateException(msg);
		} else if (!build.getProject().isConcurrentBuild() && nodeParameterDefinition.isTriggerConcurrentBuilds()) {
			final String msg = Messages.BuildWrapper_project_not_concurrent(nodeParameterDefinition.getName());
			LOGGER.severe(msg);
			throw new IllegalStateException(msg);
		}

		// trigger builds concurrent
		if (build.getProject().isConcurrentBuild()) {
			triggerAllBuildsConcurrent(build, listener);
			return new Environment() {
			};
		}

		// trigger one build after the other
		return new TriggerNextBuildEnvironment();
	}

	private void triggerAllBuildsConcurrent(AbstractBuild build, BuildListener listener) {

		final List<String> newBuildNodes = new ArrayList<String>();
		String parmaName = null;

		final ParametersAction origParamsAction = build.getAction(ParametersAction.class);
		final List<ParameterValue> origParams = origParamsAction.getParameters();
		final List<ParameterValue> newPrams = new ArrayList<ParameterValue>();
		for (ParameterValue parameterValue : origParams) {
			if (parameterValue instanceof LabelParameterValue) {
				if (parameterValue instanceof NodeParameterValue) {
					NodeParameterValue origNodeParam = (NodeParameterValue) parameterValue;
					parmaName = origNodeParam.getName();
					List<String> nextNodes = origNodeParam.getNextLabels();
					if (nextNodes != null) {
						listener.getLogger().print("next nodes: " + nextNodes);
						newBuildNodes.addAll(nextNodes);
					}
				}
			} else {
				newPrams.add(parameterValue);
			}
		}
		for (String nodeName : newBuildNodes) {
			final List<String> singleNodeList = new ArrayList<String>();
			singleNodeList.add(nodeName);
			final NodeParameterValue nodeParameterValue = new NodeParameterValue(parmaName, singleNodeList);
			List<ParameterValue> copies = new ArrayList<ParameterValue>(newPrams);
			copies.add(nodeParameterValue); // where to do the next build
			listener.getLogger().print("schedule build on node " + nodeName);
			build.getProject().scheduleBuild(0, new NextLabelCause(nodeName, build), new ParametersAction(copies));
		}
	}

	/**
	 * Environment triggering one build after the other - if the build result of
	 * the previous build is as expected.
	 */
	private class TriggerNextBuildEnvironment extends Environment {

		@Override
		public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
			triggerBuilds(build, listener);
			return true;
		}

		private void triggerBuilds(AbstractBuild build, BuildListener listener) {
			final ParametersAction origParamsAction = build.getAction(ParametersAction.class);
			final List<ParameterValue> origParams = origParamsAction.getParameters();
			final List<ParameterValue> newPrams = new ArrayList<ParameterValue>();
			boolean triggerNewBuild = false;
			NextLabelCause nextLabelCause = null;
			for (ParameterValue parameterValue : origParams) {
				if (parameterValue instanceof LabelParameterValue) {
					if (parameterValue instanceof NodeParameterValue) {
						NodeParameterValue origNodePram = (NodeParameterValue) parameterValue;
						final List<String> nextNodes = origNodePram.getNextLabels();
						if (nextNodes != null && !nextNodes.isEmpty() && shouldScheduleNextJob(build.getResult(), triggerIfResult)) {
							NodeParameterValue newNodeParam = new NodeParameterValue(origNodePram.getName(), nextNodes);
							newPrams.add(newNodeParam);
							final String nextLabel = newNodeParam.getLabel();
							if (nextLabel != null) {
								listener.getLogger().print("schedule single build on node " + nextLabel);
								nextLabelCause = new NextLabelCause(nextLabel, build);
								triggerNewBuild = true;
							} else {
								LOGGER.severe("can't trigger next build because next label could not be determined!");
							}
						}
					}
				} else {
					newPrams.add(parameterValue);
				}
			}
			if (triggerNewBuild) {
				// schedule the next build right away...
				// the ParametersAction will also contain the labels for the
				// next builds
				build.getProject().scheduleBuild(0, nextLabelCause, new ParametersAction(newPrams));
			}
		}

		/**
		 * Decides whether the next build should be triggered.
		 * 
		 * @param buildResult
		 *            the current build result
		 * @param runIfResult
		 *            the definition when to trigger the next build
		 * @return <code>true</code> if the next build shold be triggered
		 */
		private boolean shouldScheduleNextJob(Result buildResult, String runIfResult) {
			// If runIfResult is null, set it to "allCases".
			if (runIfResult == null) {
				runIfResult = "allCases";
			}
			// If runIfResult is "allCases", we're running regardless.
			if (runIfResult.equals("allCases")) {
				return true;
			} else {
				// Otherwise, we're going to need to compare against the build
				// result.

				if (runIfResult.equals("success")) {
					return ((buildResult == null) || (buildResult.isBetterOrEqualTo(Result.SUCCESS)));
				} else if (runIfResult.equals("unstable")) {
					return ((buildResult == null) || (buildResult.isBetterOrEqualTo(Result.UNSTABLE)));
				}
			}

			// If we get down here, something weird's going on. Return false.
			return false;
		}

	}

}

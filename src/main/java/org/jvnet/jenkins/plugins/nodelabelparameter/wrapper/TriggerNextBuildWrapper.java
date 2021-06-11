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

import org.apache.commons.lang.StringUtils;
import org.jvnet.jenkins.plugins.nodelabelparameter.Constants;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterValue;
import org.jvnet.jenkins.plugins.nodelabelparameter.MultipleNodeDescribingParameterDefinition;
import org.jvnet.jenkins.plugins.nodelabelparameter.NextLabelCause;

/**
 * This BuildWrapper is not marked as extension, it gets added dynamically to a
 * build by the ParameterValue implementations.
 * 
 * @author Dominik Bartholdi (imod)
 * 
 * @see LabelParameterValue#createBuildWrapper(AbstractBuild)
 * @see NodeParameterValue#createBuildWrapper(AbstractBuild)
 */
public class TriggerNextBuildWrapper extends BuildWrapper {

	private static final Logger LOGGER = Logger.getLogger(TriggerNextBuildWrapper.class.getName());

	final private MultipleNodeDescribingParameterDefinition parameterDefinition;

	public TriggerNextBuildWrapper(MultipleNodeDescribingParameterDefinition parameterDefinition) {
		this.parameterDefinition = parameterDefinition;
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

		parameterDefinition.validateBuild(build, launcher, listener);

		// trigger builds concurrent
		if (build.getProject().isConcurrentBuild()) {
			triggerAllBuildsConcurrent(build, listener);
			return new Environment() {
			};
		}

		// trigger one build after the other
		return new TriggerNextBuildEnvironment();
	}

	private void triggerAllBuildsConcurrent(AbstractBuild<?, ?> build, BuildListener listener) {

		final List<String> newBuildNodes = new ArrayList<String>();
		String parmaName = null;

		String initialBuildNode = build.getBuiltOnStr();

		final ParametersAction origParamsAction = build.getAction(ParametersAction.class);
		final List<ParameterValue> origParams = origParamsAction.getParameters();
		final List<ParameterValue> newPrams = new ArrayList<ParameterValue>();
		for (ParameterValue parameterValue : origParams) {
			if (parameterValue instanceof LabelParameterValue) {
		        LabelParameterValue origNodeParam = (LabelParameterValue) parameterValue;
				parmaName = origNodeParam.getName();
				List<String> nextNodes = origNodeParam.getNextLabels();
				if (nextNodes == null) {
					continue;
				}
				newBuildNodes.addAll(nextNodes);
				// Avoid to add the current node again
				newBuildNodes.remove(initialBuildNode);
				listener.getLogger().println("Next nodes: " + newBuildNodes);
			} else {
				newPrams.add(parameterValue);
			}
		}
		for (String nodeName : newBuildNodes) {
			final List<String> singleNodeList = new ArrayList<String>();
			singleNodeList.add(nodeName);
			final LabelParameterValue pValue = new LabelParameterValue(parmaName, singleNodeList, parameterDefinition.getNodeEligibility());
			List<ParameterValue> copies = new ArrayList<ParameterValue>(newPrams);
			copies.add(pValue); // where to do the next build
			listener.getLogger().println("Schedule build on node " + nodeName);
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

		private void triggerBuilds(AbstractBuild<?, ?> build, BuildListener listener) {
			final String initialBuildNode = build.getBuiltOnStr();
			final ParametersAction origParamsAction = build.getAction(ParametersAction.class);
			final List<ParameterValue> origParams = origParamsAction.getParameters();
			final List<ParameterValue> newPrams = new ArrayList<ParameterValue>();
			boolean triggerNewBuild = false;
			NextLabelCause nextLabelCause = null;
			for (ParameterValue parameterValue : origParams) {
				if (parameterValue instanceof LabelParameterValue) {
					LabelParameterValue origNodePram = (LabelParameterValue) parameterValue;
					List<String> nextNodes = origNodePram.getNextLabels();
					if (nextNodes == null) {
						continue;
					}
					nextNodes = new ArrayList<>(nextNodes);
					nextNodes.remove(initialBuildNode);
					if (!nextNodes.isEmpty() && shouldScheduleNextJob(build.getResult(), parameterDefinition.getTriggerIfResult())) {
					    LabelParameterValue newNodeParam = new LabelParameterValue(origNodePram.getName(), nextNodes, parameterDefinition.getNodeEligibility());
						newPrams.add(newNodeParam);
						final String nextLabel = newNodeParam.getLabel();
						if (nextLabel != null) {
							listener.getLogger().print("schedule single build on node " + nextLabel);
							nextLabelCause = new NextLabelCause(nextLabel, build);
							triggerNewBuild = true;
						} else {
						    listener.getLogger().print("ERROR: can't trigger next build because next label could not be determined!");
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
				runIfResult = Constants.ALL_CASES;
			}
			// If runIfResult is "allCases", we're running regardless.
			if (runIfResult.equals(Constants.ALL_CASES)) {
				return true;
			} else {
				// Otherwise, we're going to need to compare against the build
				// result.

				if (Constants.CASE_SUCCESS.equals(runIfResult)) {
					return ((buildResult == null) || (buildResult.isBetterOrEqualTo(Result.SUCCESS)));
				} else if (Constants.CASE_UNSTABLE.equals(runIfResult)) {
					return ((buildResult == null) || (buildResult.isBetterOrEqualTo(Result.UNSTABLE)));
				}
			}

			// If we get down here, something weird's going on. Return false.
			return false;
		}

	}

}

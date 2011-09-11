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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterValue;
import org.jvnet.jenkins.plugins.nodelabelparameter.NextLabelCause;
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

	private String triggerIfResult;

	public TriggerNextBuildWrapper(String triggerIfResult) {
		this.triggerIfResult = triggerIfResult;
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
		// TODO add support for concurrent execution on different nodes
		return new TriggerNextBuildEnvironment();
	}

	private class TriggerNextBuildEnvironment extends Environment {

		@Override
		public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
			final ParametersAction origParamsAction = build.getAction(ParametersAction.class);
			final List<ParameterValue> origParams = origParamsAction.getParameters();
			final List<ParameterValue> newPrams = new ArrayList<ParameterValue>();
			boolean triggerNewBuild = false;
			NextLabelCause nextLabelCause = null;
			for (ParameterValue parameterValue : origParams) {
				if (parameterValue instanceof LabelParameterValue) {
					if (parameterValue instanceof LabelParameterValue) {
						NodeParameterValue origNodePram = (NodeParameterValue) parameterValue;
						final List<String> nextNodes = origNodePram.getNextLabels();
						if (nextNodes != null && !nextNodes.isEmpty() && shouldScheduleNextJob(build.getResult(), triggerIfResult)) {
							NodeParameterValue newNodeParam = new NodeParameterValue(origNodePram.getName(), nextNodes);
							newPrams.add(newNodeParam);
							final String nextLabel = newNodeParam.getLabel();
							if (nextLabel != null) {
								LOGGER.log(Level.FINE, "schedule build for label {0}", nextLabel);
								nextLabelCause = new NextLabelCause(nextLabel);
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
				build.getProject().scheduleBuild(0, nextLabelCause, new ParametersAction(newPrams));
			}
			return true;
		}

		/**
		 * decides whether the next build should be triggered
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

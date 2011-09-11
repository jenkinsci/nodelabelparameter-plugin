/**
 * 
 */
package org.jvnet.jenkins.plugins.nodelabelparameter.notifier;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.ParameterValue;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ParametersAction;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.jvnet.jenkins.plugins.nodelabelparameter.LabelBadgeAction;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterValue;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterValue;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author domi
 * 
 */
public class NextNodeBuildNotifier extends Notifier {

	private static final Logger LOGGER = Logger.getLogger(NextNodeBuildNotifier.class.getName());

	private String triggerIfResult;

	@DataBoundConstructor
	public NextNodeBuildNotifier(String triggerIfResult) {
		this.triggerIfResult = triggerIfResult;
	}

	/**
	 * @see hudson.tasks.BuildStep#getRequiredMonitorService()
	 */
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		final ParametersAction origParamsAction = build.getAction(ParametersAction.class);
		if (origParamsAction == null) {
			LOGGER.warning("activating " + this.getClass().getSimpleName() + " does not make sense if there is no parameter defined");
			return true;
		}
		final List<ParameterValue> origParams = origParamsAction.getParameters();
		final List<ParameterValue> newPrams = new ArrayList<ParameterValue>();
		boolean triggerNewBuild = false;
		LabelBadgeAction badgeAction = null;
		NextLabelCause nextLabelCause = null;
		for (ParameterValue parameterValue : origParams) {
			if (parameterValue instanceof LabelParameterValue) {
				final LabelBadgeAction firstBadgeAction = build.getAction(LabelBadgeAction.class);
				if (firstBadgeAction == null) {
					// ensure each build has a badge (also the first build in a
					// list
					// of builds)
					build.addAction(new LabelBadgeAction(((LabelParameterValue) parameterValue).getLabel()));
				}
				if (parameterValue instanceof LabelParameterValue) {
					NodeParameterValue origNodePram = (NodeParameterValue) parameterValue;
					final List<String> nextNodes = origNodePram.getNextLabels();
					if (nextNodes != null && !nextNodes.isEmpty() && shouldScheduleNextJob(build.getResult(), getTriggerIfResult())) {
						NodeParameterValue newNodeParam = new NodeParameterValue(origNodePram.getName(), nextNodes);
						newPrams.add(newNodeParam);
						final String nextLabel = newNodeParam.getLabel();
						if (nextLabel != null) {
							LOGGER.log(Level.FINE, "schedule build for label {}", nextLabel);
							badgeAction = new LabelBadgeAction(nextLabel);
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
			build.getProject().scheduleBuild(0, nextLabelCause, new ParametersAction(newPrams), badgeAction);
		}
		return true;
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public DescriptorImpl() {
			super(NextNodeBuildNotifier.class);
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return Messages.NextNodeBuildNotifier_displayName();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		/**
		 * Creates a new instance of {@link NaginatorPublisher} from a submitted
		 * form.
		 */
		@Override
		public Notifier newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			return req.bindJSON(NextNodeBuildNotifier.class, formData);
		}
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

	/**
	 * @return the triggerIfResult
	 */
	public String getTriggerIfResult() {
		return triggerIfResult;
	}

}

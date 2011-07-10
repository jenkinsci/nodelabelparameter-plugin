package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.ParametersAction;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;

import java.io.IOException;

import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterValue;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * As this plugin is build against Jenkins1.398 and dynamic nodelabel assignment
 * was only introduced with Jenkins1.417, this extension is marked as optional!
 * 
 * @author domi
 * 
 */
public class NodeLabelBuildParameter extends AbstractBuildParameters {

	public final String name;
	public final String nodeLabel;

	@DataBoundConstructor
	public NodeLabelBuildParameter(String name, String nodeLabel) {
		this.name = name;
		this.nodeLabel = nodeLabel;
	}

	public Action getAction(AbstractBuild<?, ?> build, TaskListener listener)
			throws IOException, InterruptedException {
		EnvVars env = build.getEnvironment(listener);
		String labelExpanded = env.expand(nodeLabel);

		LabelParameterValue parameterValue = new LabelParameterValue(name,
				labelExpanded);
		listener.getLogger().println("define: " + parameterValue);

		return new ParametersAction(parameterValue);
	}

	@Extension(optional = true)
	public static class DescriptorImpl extends
			Descriptor<AbstractBuildParameters> {

		// force loading of dependent class to disable extension early
//		static {
//			try {
//				@SuppressWarnings("unused")
//				Class<?> c = LabelParameterValue.class;
//			} catch (Throwable e) {
//				throw new NoClassDefFoundError(
//						"'LabelParameterValue' - nodelabelparameter-plugin not installed, disable NodeLabelBuildParameter for parameterized-trigger-plugin");
//			}
//		}

		
		
		@Override
		public String getDisplayName() {
			return "NodeLabel parameter";
		}
	}

}

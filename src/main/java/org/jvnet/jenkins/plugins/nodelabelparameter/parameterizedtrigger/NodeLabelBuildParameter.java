package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.ParametersAction;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;

import java.io.IOException;

import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterValue;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * As this plugin is build against Jenkins1.398 and dynamic nodelabel assignment was only introduced with Jenkins1.417, this extension is marked as optional!
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

    public Action getAction(AbstractBuild<?, ?> build, TaskListener listener) throws IOException, InterruptedException {
        String labelExpanded = nodeLabel;
        try {
            labelExpanded = TokenMacro.expandAll(build, listener, labelExpanded);
        } catch (MacroEvaluationException e) {
            labelExpanded = nodeLabel;
            e.printStackTrace(listener.getLogger());
        }
        LabelParameterValue parameterValue = new LabelParameterValue(name, labelExpanded, false, false);
        listener.getLogger().println("define: " + parameterValue);

        return new ParametersAction(parameterValue);
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends Descriptor<AbstractBuildParameters> {
        @Override
        public String getDisplayName() {
            return "NodeLabel parameter";
        }
    }

}

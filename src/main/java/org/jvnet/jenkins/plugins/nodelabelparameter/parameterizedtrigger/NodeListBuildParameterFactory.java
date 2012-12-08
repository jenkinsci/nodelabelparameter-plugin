package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactory;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactoryDescriptor;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.collect.Lists;

/**
 * A build parameter factory generating NodeLabelParameters for each node matching a label
 */
public class NodeListBuildParameterFactory extends AbstractBuildParameterFactory {

    public final String name;
    public final String nodeListString;

    @DataBoundConstructor
    public NodeListBuildParameterFactory(String name, String nodeListString) {
        this.name = name;
        this.nodeListString = nodeListString;
    }

    @Override
    public List<AbstractBuildParameters> getParameters(AbstractBuild<?, ?> build, TaskListener listener) throws IOException, InterruptedException, AbstractBuildParameters.DontTriggerException {
        String nodeListStringExpanded = nodeListString;
        try {
            nodeListStringExpanded = TokenMacro.expandAll(build, listener, nodeListStringExpanded);
        } catch (MacroEvaluationException e) {
            nodeListStringExpanded = nodeListString;
            e.printStackTrace(listener.getLogger());
        }

        List<AbstractBuildParameters> params = Lists.newArrayList();

        if (StringUtils.isBlank(nodeListStringExpanded)) {
            listener.getLogger().println("[WARN] no node name was given! [" + nodeListString + "], can't trigger other project");
        } else {

            String nodes[] = nodeListStringExpanded.trim().split(",");
            if (nodes == null || nodes.length == 0) {
                params.add(new NodeLabelBuildParameter(name, nodeListStringExpanded));
            } else {
                for (int i = 0; i < nodes.length; i++) {
                    params.add(new NodeLabelBuildParameter(name, nodes[i]));
                }
            }
        }

        return params;
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends AbstractBuildParameterFactoryDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.NodeListBuildParameterFactory_displayName();
        }

        /**
         * Autocompletion method, called by UI to support user filling the form
         * 
         * @param value
         * @return
         */
        // public AutoCompletionCandidates doAutoCompleteNodeList(@QueryParameter String value) {
        // AutoCompletionCandidates candidates = new AutoCompletionCandidates();
        //
        // for (Node n : Jenkins.getInstance().getNodes()) {
        // candidates.add(n.getSelfLabel().getExpression());
        // }
        //
        // return candidates;
        // }
    }

}

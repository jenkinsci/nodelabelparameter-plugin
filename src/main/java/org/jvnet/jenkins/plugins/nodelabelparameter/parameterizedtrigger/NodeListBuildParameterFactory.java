package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import hudson.Extension;
import hudson.Util;
import hudson.model.AutoCompletionCandidates;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Node;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactory;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactoryDescriptor;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * A build parameter factory generating NodeLabelParameters for each node matching a label
 */
public class NodeListBuildParameterFactory extends AbstractBuildParameterFactory {

    private static final Logger LOGGER = Logger.getLogger(NodeListBuildParameterFactory.class.getName());

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

        List<AbstractBuildParameters> params = new ArrayList<>();

        if (StringUtils.isBlank(nodeListStringExpanded)) {
            listener.getLogger().println("[WARN] no node name was given! [" + nodeListString + "], can't trigger other project");
        } else {

            String[] nodes = nodeListStringExpanded.trim().split(",");
            if (nodes == null || nodes.length == 0) {
                params.add(new NodeLabelBuildParameter(name, nodeListStringExpanded));
            } else {
                for (String node : nodes) {
                    params.add(new NodeLabelBuildParameter(name, node));
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
        public AutoCompletionCandidates doAutoCompleteNodeListString(@QueryParameter String value) {
            final AutoCompletionCandidates candidates = new AutoCompletionCandidates();

            for (Node n : Jenkins.get().getNodes()) {
                candidates.add(n.getSelfLabel().getExpression());
            }

            return candidates;

        }

        /**
         * Form validation method.
         */
        public FormValidation doCheckNodeListString(@AncestorInPath Item project, @QueryParameter String value) {
            if (!project.hasPermission(Item.CONFIGURE))
                return FormValidation.ok();

            StringTokenizer tokens = new StringTokenizer(Util.fixNull(value), ",");
            boolean hasProjects = false;
            while (tokens.hasMoreTokens()) {
                String nodeName = tokens.nextToken().trim();
                if (StringUtils.isNotBlank(nodeName)) {
                    final Node node = Jenkins.get().getNode(nodeName);
                    if (node == null) {
                        return FormValidation.error(Messages.NodeListBuildParameterFactory_nodeNotFound(nodeName));
                    }
                    hasProjects = true;
                }
            }
            if (!hasProjects) {
                return FormValidation.error(Messages.NodeListBuildParameterFactory_nodeNotDefined());
            }

            return FormValidation.ok();
        }

    }
}

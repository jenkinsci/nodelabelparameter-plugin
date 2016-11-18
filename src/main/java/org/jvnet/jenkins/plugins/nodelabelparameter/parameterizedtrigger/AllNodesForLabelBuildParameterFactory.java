package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactory;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactoryDescriptor;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jenkins.model.Jenkins;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterValue;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.jvnet.jenkins.plugins.nodelabelparameter.NodeUtil;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * A build parameter factory generating NodeLabelParameters for each node matching a label
 */
public class AllNodesForLabelBuildParameterFactory extends AbstractBuildParameterFactory {
    public final String name;
    public final String nodeLabel;
    public final boolean ignoreOfflineNodes;

    private static final Function<Node, String> SELF_LABEL_FUNCTION = new Function<Node, String>() {
        public String apply(Node node) {
            return node != null && node.getSelfLabel() != null ? node.getSelfLabel().getName() : null;
        }
    };

    @DataBoundConstructor
    public AllNodesForLabelBuildParameterFactory(String name, String nodeLabel, boolean ignoreOfflineNodes) {
        this.name = name;
        this.nodeLabel = nodeLabel;
        this.ignoreOfflineNodes = ignoreOfflineNodes;
    }

    @Override
    public List<AbstractBuildParameters> getParameters(AbstractBuild<?, ?> build, TaskListener listener) throws IOException, InterruptedException, AbstractBuildParameters.DontTriggerException {
        String labelExpanded = nodeLabel;
        try {
            labelExpanded = TokenMacro.expandAll(build, listener, labelExpanded);
        } catch (MacroEvaluationException e) {
            labelExpanded = nodeLabel;
            e.printStackTrace(listener.getLogger());
        }
        
        listener.getLogger().println("Getting all nodes with label: " + labelExpanded);
        Set<Node> nodes = Jenkins.getActiveInstance().getLabel(labelExpanded).getNodes();

        List<AbstractBuildParameters> params = Lists.newArrayList();

        if (nodes == null || nodes.isEmpty()) {
            listener.getLogger().println("Found no nodes");
            params.add(new NodeLabelBuildParameter(name, labelExpanded));
        } else {
            List<String> selfLabels = Lists.transform(new ArrayList<Node>(nodes), SELF_LABEL_FUNCTION);
            listener.getLogger().println("Found nodes: " + String.valueOf(selfLabels));
            for (Node node : nodes) {
                final String nodeSelfLabel = node.getSelfLabel().getName();
                if (ignoreOfflineNodes) {
                    if(NodeUtil.isNodeOnline(nodeSelfLabel)) {
                        params.add(new NodeLabelBuildParameter(name, nodeSelfLabel));
                    } else {
                        listener.getLogger().println(Messages.NodeListBuildParameterFactory_skippOfflineNode(nodeSelfLabel));  
                    }
                } else {
                    params.add(new NodeLabelBuildParameter(name, nodeSelfLabel));
                }
            }
            if(params.isEmpty()) {
                params.add(new NodeLabelBuildParameter(name, labelExpanded));
                listener.getLogger().println(Messages.NodeListBuildParameterFactory_noOnlineNodeFound(labelExpanded));
            }
        }

        return params;
    }
    
    public boolean isIgnoreOfflineNodes() {
        return ignoreOfflineNodes;
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends AbstractBuildParameterFactoryDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.AllNodesForLabelBuildParameterFactory_displayName();
        }
    }

}

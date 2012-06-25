package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import hudson.Extension;
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

import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * A build parameter factory generating NodeLabelParameters for each node matching a label
 */
public class AllNodesForLabelBuildParameterFactory extends AbstractBuildParameterFactory {
    public final String name;
    public final String nodeLabel;

    private static final Function<Node, String> NODE_NAME_FUNCTION = new Function<Node, String>() {
        public String apply(Node from) {
            return from.getDisplayName();
        }
    };
    
    @DataBoundConstructor
    public AllNodesForLabelBuildParameterFactory(String name, String nodeLabel) {
        this.name = name;
        this.nodeLabel = nodeLabel;
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
        Set<Node> nodes = Hudson.getInstance().getLabel(labelExpanded).getNodes();
        List<String> nodeNames = Lists.transform(new ArrayList<Node>(nodes), NODE_NAME_FUNCTION);
        listener.getLogger().println("Found nodes: " + String.valueOf(nodeNames));
        List<AbstractBuildParameters> params = Lists.newArrayList();
        if (nodes == null || nodes.isEmpty()) {
            params.add(new NodeLabelBuildParameter(name, labelExpanded));
        } else {
            for(Node node : nodes) {
                params.add(new NodeLabelBuildParameter(name, node.getNodeName()));
            }
        }

		return params;
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends
            AbstractBuildParameterFactoryDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.AllNodesForLabelBuildParameterFactory_displayName();
        }
    }


}

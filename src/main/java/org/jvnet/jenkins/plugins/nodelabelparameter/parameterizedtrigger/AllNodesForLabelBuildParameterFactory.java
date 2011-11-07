package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import com.google.common.collect.Lists;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactory;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactoryDescriptor;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * A build parameter factory generating NodeLabelParameters for each node matching a label
 */
public class AllNodesForLabelBuildParameterFactory extends AbstractBuildParameterFactory {
    public final String name;
    public final String nodeLabel;

    @DataBoundConstructor
    public AllNodesForLabelBuildParameterFactory(String name, String nodeLabel) {
        this.name = name;
        this.nodeLabel = nodeLabel;
    }

    @Override
    public List<AbstractBuildParameters> getParameters(AbstractBuild<?, ?> build, TaskListener listener) throws IOException, InterruptedException, AbstractBuildParameters.DontTriggerException {
        EnvVars env = build.getEnvironment(listener);
		String labelExpanded = env.expand(nodeLabel);

		listener.getLogger().println("Getting all nodes with label: " + labelExpanded);
        Set<Node> nodes = Hudson.getInstance().getLabel(labelExpanded).getNodes();
        listener.getLogger().println("Found nodes: " + String.valueOf(nodes));
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

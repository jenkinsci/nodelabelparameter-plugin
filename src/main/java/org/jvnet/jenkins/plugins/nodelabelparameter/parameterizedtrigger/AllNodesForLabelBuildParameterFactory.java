package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import static com.google.common.collect.Lists.transform;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactory;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactoryDescriptor;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A build parameter factory generating NodeLabelParameters for each node matching a label
 */
public class AllNodesForLabelBuildParameterFactory extends AbstractBuildParameterFactory {
    public final String name;
    public final String nodeLabel;

    private static final Function<Node, String> PROJECT_NAME_FUNCTION = new Function<Node, String>() {
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
        EnvVars env = build.getEnvironment(listener);
		String labelExpanded = env.expand(nodeLabel);

		listener.getLogger().println("Getting all nodes with label: " + labelExpanded);
        Set<Node> nodes = Hudson.getInstance().getLabel(labelExpanded).getNodes();
        List<String> nodeNames = Lists.transform(new ArrayList<Node>(nodes), PROJECT_NAME_FUNCTION);
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

package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.EnvVars;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.Iterables;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Hudson.class)
public class AllNodesForLabelBuildParameterFactoryUnitTest {

    @Mock
    Label mockLabel;
    @Mock
    AbstractBuild<?,?> build;
    @Mock
    TaskListener listener;
    @Mock
    PrintStream listenerLogger;

    @Test
    public void shouldGetParameterForEachMatchingNode() throws Exception {
        String label="label";
        setupNodesForLabel(
                label,
                createNodesWithNames("node1", "node2", "node3"));
        setupBuild();

        AllNodesForLabelBuildParameterFactory allNodesFactory = new AllNodesForLabelBuildParameterFactory("RST", label, false);
        List<AbstractBuildParameters> parameters = allNodesFactory.getParameters(build, listener);

        Set<String> nodeNames = new HashSet<String>();
        for (AbstractBuildParameters parameter : parameters) {
            nodeNames.add(((NodeLabelBuildParameter) parameter).nodeLabel);
        }

        assertThat(nodeNames).containsOnly("node1", "node2", "node3");
    }

    @Test
    public void noMatchingNodeShouldYieldSameLabel() throws Exception {
        String label="label";
        setupNodesForLabel(label, Collections.<Node>emptySet());
        setupBuild();

        AllNodesForLabelBuildParameterFactory allNodesFactory = new AllNodesForLabelBuildParameterFactory("LABEL", label, false);
        List<AbstractBuildParameters> parameters = allNodesFactory.getParameters(build, listener);

        NodeLabelBuildParameter parameter = (NodeLabelBuildParameter) Iterables.getOnlyElement(parameters);
        assertThat(parameter.nodeLabel).isEqualTo(label);
    }

    private Node createNodeWithName(String name) {
        Node node = mock(Node.class);
        when(node.getNodeName()).thenReturn(name);
        when(node.getSelfLabel()).thenReturn(new LabelAtom(name));
        return node;
    }

    private Set<Node> createNodesWithNames(String... names) {
        Set<Node> nodes = new HashSet<Node>();
        for (String name : names) {
            nodes.add(createNodeWithName(name));
        }
        return nodes;
    }

    private void setupBuild() throws IOException, InterruptedException {
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
        when(listener.getLogger()).thenReturn(listenerLogger);
    }

    private void setupNodesForLabel(String label, Set<Node> nodes) {
        PowerMockito.mockStatic(Hudson.class);
        Hudson hudsonMock = Mockito.mock(Hudson.class);
        when(Hudson.getInstance()).thenReturn(hudsonMock);
        when(hudsonMock.getLabel(label)).thenReturn(mockLabel);
        when(mockLabel.getNodes()).thenReturn(nodes);
    }
}

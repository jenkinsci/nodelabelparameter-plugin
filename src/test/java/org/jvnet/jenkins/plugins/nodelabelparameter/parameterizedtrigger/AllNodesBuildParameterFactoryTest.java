package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import static org.junit.Assert.*;

import hudson.model.Computer;
import hudson.model.Node;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.slaves.DumbSlave;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jenkins.model.Jenkins;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class AllNodesBuildParameterFactoryTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private AllNodesBuildParameterFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new AllNodesBuildParameterFactory();
    }

    @After
    public void tearDown() throws Exception {
        Computer[] computers = Jenkins.get().getComputers();
        for (Computer computer : computers) {
            computer.setTemporarilyOffline(false, null);
        }
    }

    @Test
    public void getParameters_withAllNodesOnline() throws Exception {
        DumbSlave node1 = j.createOnlineSlave();
        DumbSlave node2 = j.createOnlineSlave();

        List<AbstractBuildParameters> params = factory.getParameters(null, j.createTaskListener());

        // Parameters should include 2 agents and the Jenkins controller (built-in)
        assertEquals(3, params.size());
    }

    @Test
    public void getParameters_withAllNodesOffline() throws Exception {
        DumbSlave node1 = j.createSlave();
        DumbSlave node2 = j.createSlave();

        Objects.requireNonNull(node1.toComputer()).disconnect(null);
        Objects.requireNonNull(node2.toComputer()).disconnect(null);

        List<AbstractBuildParameters> params = factory.getParameters(null, j.createTaskListener());
        // Parameters should only include the controller, since 2 agents are offline
        // AllNodes factory only returns online agents with 1 or more executors
        assertEquals(1, params.size());
    }

    @Test
    public void getParameters_withMixedNodeStates() throws Exception {
        DumbSlave node1 = j.createOnlineSlave();
        DumbSlave node2 = j.createSlave();

        Objects.requireNonNull(node2.toComputer()).disconnect(null);

        List<AbstractBuildParameters> params = factory.getParameters(null, j.createTaskListener());
        // Parameters should include the controller and the 1 online agent
        assertEquals(2, params.size());
    }

    @Test
    public void getParameters_withNoNodes() throws Exception {
        Computer[] computers = Jenkins.get().getComputers();
        for (Computer computer : computers) {
            computer.setTemporarilyOffline(true, null);
        }

        List<AbstractBuildParameters> params = factory.getParameters(null, j.createTaskListener());

        assertTrue(params.isEmpty());
    }

    @Test
    public void getParameters_setOneExecutorAgentToZero() throws Exception {
        DumbSlave node1 = j.createOnlineSlave();
        DumbSlave node2 = j.createOnlineSlave();

        node1.setNumExecutors(0);

        List<AbstractBuildParameters> params = factory.getParameters(null, j.createTaskListener());

        //    node1 will not be able to execute any builds but it will be included in the list of nodes
        assertEquals(3, params.size());

        // Create a list of nodes that can execute builds
        List<Node> buildableNodes = new ArrayList<>();
        for (Computer c : Jenkins.get().getComputers()) {
            Node n = c.getNode();
            if (n != null && c.isOnline() && c.getNumExecutors() > 0) {
                buildableNodes.add(n);
            }
        }
        // Check that the number of buildable nodes is equal to the number of parameters
        assertEquals(buildableNodes.size(), params.size());
    }
}

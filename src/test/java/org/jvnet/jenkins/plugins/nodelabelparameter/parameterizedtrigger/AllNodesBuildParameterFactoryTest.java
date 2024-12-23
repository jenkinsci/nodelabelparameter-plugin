package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import static org.junit.Assert.*;

import hudson.model.Computer;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.slaves.DumbSlave;
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

        // trigger build on Jenkins (built-in) included in params.size() so added 1 in expected
        assertEquals(3, params.size());
    }

    @Test
    public void getParameters_withAllNodesOffline() throws Exception {
        DumbSlave node1 = j.createSlave();
        DumbSlave node2 = j.createSlave();

        Objects.requireNonNull(node1.toComputer()).disconnect(null);
        Objects.requireNonNull(node2.toComputer()).disconnect(null);

        List<AbstractBuildParameters> params = factory.getParameters(null, j.createTaskListener());
        //    trigger build on Jenkins (built-in)
        assertEquals(1, params.size());
    }

    @Test
    public void getParameters_withMixedNodeStates() throws Exception {
        DumbSlave node1 = j.createOnlineSlave();
        DumbSlave node2 = j.createSlave();

        Objects.requireNonNull(node2.toComputer()).disconnect(null);

        List<AbstractBuildParameters> params = factory.getParameters(null, j.createTaskListener());
        //    trigger build on Jenkins (built-in)
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
}

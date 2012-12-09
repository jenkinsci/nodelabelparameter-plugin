package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.FreeStyleProject;
import hudson.model.Node;
import hudson.model.Project;
import hudson.model.Queue;
import hudson.model.labels.LabelAtom;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactory;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BlockingBehaviour;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.slaves.DumbSlave;
import hudson.util.RunList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jvnet.hudson.test.HudsonTestCase;

import com.google.common.collect.ImmutableList;

/**
 * @author wolfs
 */
public class AllNodesForLabelBuildParameterFactoryTest extends HudsonTestCase {

    public void testLabelFactoryNonBlocking() throws Exception {
		// create a slave with a given label to execute projectB on
        String label = "label";
        List<DumbSlave> slaves = createSlaves(label,3);

        FreeStyleProject projectB = createFreeStyleProject();
        projectB.setQuietPeriod(1);

		// create projectA, which triggers projectB with a given label parameter
		Project<?, ?> projectA = createFreeStyleProject();
        addLabelParameterFactory(projectA, projectB, label);

        projectA.scheduleBuild2(0);

        waitUntilNoActivity();

        assertBuiltOnEachSlave(projectB, slaves);

        teardownSlaves(slaves);
    }

    public void testLabelFactoryBlocking() throws Exception {
		// create a slave with a given label to execute projectB on
        String label = "label";
        List<DumbSlave> slaves = createSlaves(label,2);

        FreeStyleProject projectB = createFreeStyleProject();
        projectB.setQuietPeriod(1);

		// create projectA, which triggers projectB with a given label parameter
		Project<?, ?> projectA = createFreeStyleProject();
        addBlockingLabelParameterFactory(projectA, projectB, label);

        projectA.scheduleBuild2(0);

        waitUntilNoActivity();

        assertBuiltOnEachSlave(projectB, slaves);

        teardownSlaves(slaves);
    }

    public void testNoSlavesWithLabel() throws Exception {
        Project<?, ?> projectA = createFreeStyleProject();
        FreeStyleProject projectB = createFreeStyleProject();
        projectB.setQuietPeriod(0);

        String label = "label";
        addBlockingLabelParameterFactory(projectA, projectB, label);

        projectA.scheduleBuild2(0);

        // Wait for Project to be in Queue
        Thread.sleep(1000);

        Queue.Item projectBInQueue = hudson.getQueue().getItem(projectB);

        assertNotNull(projectBInQueue);
        assertEquals(label, projectBInQueue.getAssignedLabel().getName());


    }

    private void teardownSlaves(List<DumbSlave> slaves) throws IOException {
        for (DumbSlave slave : slaves) {
            hudson.removeNode(slave);
        }
    }

    private void assertBuiltOnEachSlave(FreeStyleProject projectB, List<DumbSlave> slaves) {
        RunList<FreeStyleBuild> builds = projectB.getBuilds();
        assertEquals(slaves.size(), builds.size());

        Set<Node> nodes = new HashSet<Node>();
        for (FreeStyleBuild build : builds) {
            nodes.add(build.getBuiltOn());
        }

        assertEquals(slaves.size(), nodes.size());
        for (DumbSlave slave : slaves) {
            assertTrue(nodes.contains(slave));
        }
    }

    private void addLabelParameterFactory(Project<?, ?> projectA, FreeStyleProject projectB, String label) throws IOException {
        addLabelParameterFactory(projectA, projectB, null, label);
    }

    private void addBlockingLabelParameterFactory(Project<?, ?> projectA, FreeStyleProject projectB, String label) throws IOException {
        addLabelParameterFactory(projectA, projectB, new BlockingBehaviour(Result.FAILURE, Result.UNSTABLE, Result.FAILURE), label);
    }

    private void addLabelParameterFactory(
            Project<?, ?> projectA,
            FreeStyleProject projectB,
            BlockingBehaviour blockingBehaviour,
            String label) throws IOException {
        projectA.getBuildersList().add(
                new TriggerBuilder(new BlockableBuildTriggerConfig(projectB.getName(),
                        blockingBehaviour,
                        ImmutableList.<AbstractBuildParameterFactory>of(new AllNodesForLabelBuildParameterFactory(
                                "LABEL", label)),
                        Collections.<AbstractBuildParameters>emptyList())));
    }

    private List<DumbSlave> createSlaves(String label, int num) throws Exception {
        List<DumbSlave> slaves = new ArrayList<DumbSlave>();
        for (int i = 0; i < num; i++) {
            DumbSlave slave = createOnlineSlave(new LabelAtom(label));
            slave.setMode(Node.Mode.EXCLUSIVE);
            slaves.add(slave);
        }
        return slaves;
    }

}

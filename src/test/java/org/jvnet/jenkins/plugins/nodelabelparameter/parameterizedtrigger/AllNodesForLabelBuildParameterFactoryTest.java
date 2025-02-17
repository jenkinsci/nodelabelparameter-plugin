package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Node;
import hudson.model.Project;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.labels.LabelAtom;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BlockingBehaviour;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.slaves.DumbSlave;
import hudson.util.RunList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * @author wolfs
 */
@WithJenkins
class AllNodesForLabelBuildParameterFactoryTest {

    @Test
    void testLabelFactoryNonBlocking(JenkinsRule j) throws Exception {
        // create a slave with a given label to execute projectB on
        String label = "label";
        List<DumbSlave> slaves = createSlaves(j, label, 3);

        FreeStyleProject projectB = j.createFreeStyleProject();
        projectB.setQuietPeriod(1);

        // create projectA, which triggers projectB with a given label parameter
        Project<?, ?> projectA = j.createFreeStyleProject();
        addLabelParameterFactory(projectA, projectB, label);

        projectA.scheduleBuild2(0);

        j.waitUntilNoActivity();

        assertBuiltOnEachSlave(projectB, slaves);
    }

    @Test
    void testLabelFactoryBlocking(JenkinsRule j) throws Exception {
        // create a slave with a given label to execute projectB on
        String label = "label";
        List<DumbSlave> slaves = createSlaves(j, label, 2);

        FreeStyleProject projectB = j.createFreeStyleProject();
        projectB.setQuietPeriod(1);

        // create projectA, which triggers projectB with a given label parameter
        Project<?, ?> projectA = j.createFreeStyleProject();
        addBlockingLabelParameterFactory(projectA, projectB, label);

        projectA.scheduleBuild2(0);

        j.waitUntilNoActivity();

        assertBuiltOnEachSlave(projectB, slaves);
    }

    @Test
    void testNoSlavesWithLabel(JenkinsRule j) throws Exception {
        Project<?, ?> projectA = j.createFreeStyleProject();
        FreeStyleProject projectB = j.createFreeStyleProject();
        projectB.setQuietPeriod(0);

        String label = "label";
        addBlockingLabelParameterFactory(projectA, projectB, label);

        projectA.scheduleBuild2(0);

        // Wait for Project to be in Queue
        // Sleep up to 1 second
        int counter = 0;
        do {
            Thread.sleep(103); // give time to finish (0.1 second)
        } while (++counter < 10 && j.jenkins.getQueue().getItem(projectB) == null);

        Queue.Item projectBInQueue = j.jenkins.getQueue().getItem(projectB);

        assertNotNull(projectBInQueue);
        assertEquals(label, projectBInQueue.getAssignedLabel().getName());
        // Cancel the job in the queue and wait for end of activity
        j.jenkins.getQueue().cancel(projectBInQueue);
        j.waitUntilNoActivity();
        assertThat("Full sleep time consumed", counter, is(lessThan(10)));
    }

    private static void assertBuiltOnEachSlave(FreeStyleProject projectB, List<DumbSlave> slaves) {
        RunList<FreeStyleBuild> builds = projectB.getBuilds();
        assertEquals(slaves.size(), builds.size());

        Set<Node> nodes = new HashSet<>();
        for (FreeStyleBuild build : builds) {
            nodes.add(build.getBuiltOn());
        }

        assertEquals(slaves.size(), nodes.size());
        for (DumbSlave slave : slaves) {
            assertTrue(nodes.contains(slave));
        }
    }

    private static void addLabelParameterFactory(Project<?, ?> projectA, FreeStyleProject projectB, String label) {
        addLabelParameterFactory(projectA, projectB, null, label);
    }

    private static void addBlockingLabelParameterFactory(
            Project<?, ?> projectA, FreeStyleProject projectB, String label) {
        addLabelParameterFactory(
                projectA, projectB, new BlockingBehaviour(Result.FAILURE, Result.UNSTABLE, Result.FAILURE), label);
    }

    private static void addLabelParameterFactory(
            Project<?, ?> projectA, FreeStyleProject projectB, BlockingBehaviour blockingBehaviour, String label) {
        projectA.getBuildersList()
                .add(new TriggerBuilder(new BlockableBuildTriggerConfig(
                        projectB.getName(),
                        blockingBehaviour,
                        Collections.singletonList(new AllNodesForLabelBuildParameterFactory("LABEL", label, false)),
                        Collections.emptyList())));
    }

    private static List<DumbSlave> createSlaves(JenkinsRule j, String label, int num) throws Exception {
        List<DumbSlave> slaves = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            DumbSlave slave = j.createSlave(new LabelAtom(label));
            slave.setMode(Node.Mode.EXCLUSIVE);
            slaves.add(slave);
        }
        return slaves;
    }
}

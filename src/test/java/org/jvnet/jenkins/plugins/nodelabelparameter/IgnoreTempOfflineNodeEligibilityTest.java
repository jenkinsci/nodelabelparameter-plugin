package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.DescriptorExtensionList;
import hudson.model.Node;
import hudson.slaves.DumbSlave;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.IgnoreTempOfflineNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.NodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.NodeEligibility.NodeEligibilityDescriptor;

@WithJenkins
class IgnoreTempOfflineNodeEligibilityTest {

    private final IgnoreTempOfflineNodeEligibility ignoreTempOfflineNodeEligibility =
            new IgnoreTempOfflineNodeEligibility();
    private final Random random = new Random();

    private static JenkinsRule j;

    private static DumbSlave onlineNode1;

    @BeforeAll
    static void setUp(JenkinsRule rule) throws Exception {
        j = rule;
        onlineNode1 = j.createOnlineSlave();
    }

    @Test
    void testGetComputer() throws Exception {
        // Does not matter if executors on the Jenkins controller are enabled
        j.jenkins.setNumExecutors(random.nextBoolean() ? 1 : 0);

        // Null node is never eligible
        Node node = null;
        assertFalse(ignoreTempOfflineNodeEligibility.isEligible(node));
        // Non-existent node name is never eligible
        assertFalse(ignoreTempOfflineNodeEligibility.isEligible("not-a-valid-node"));

        // Online node is always eligible
        assertTrue(ignoreTempOfflineNodeEligibility.isEligible(onlineNode1));
        // Online node name is always eligible
        assertTrue(ignoreTempOfflineNodeEligibility.isEligible(onlineNode1.getNodeName()));
    }

    @Test
    void testGetComputerWithControllerExecutor() throws Exception {
        // Enable executors on the Jenkins controller
        j.jenkins.setNumExecutors(1);

        // Node of the Jenkins controller is eligible because it has executors
        assertTrue(ignoreTempOfflineNodeEligibility.isEligible(j.jenkins));
        // Empty node name is eligible because empty string matches the controller
        assertTrue(ignoreTempOfflineNodeEligibility.isEligible(""));
        // Node name of the Jenkins controller is eligible because it has executors
        assertTrue(ignoreTempOfflineNodeEligibility.isEligible("built-in"));
    }

    @Test
    void testGetComputerWithoutControllerExecutor() throws Exception {
        // Disable executors on the Jenkins controller
        j.jenkins.setNumExecutors(0);

        // Node of the Jenkins controller is not eligible because it has no executors
        assertFalse(ignoreTempOfflineNodeEligibility.isEligible(j.jenkins));
        // Empty node name is not eligible because empty string matches the controller
        assertFalse(ignoreTempOfflineNodeEligibility.isEligible(""));
        // Node name of the Jenkins controller is not eligible because it has no executors
        assertFalse(ignoreTempOfflineNodeEligibility.isEligible("built-in"));
    }

    @Test
    void testGetDescriptor() {
        NodeEligibilityDescriptor descriptor = ignoreTempOfflineNodeEligibility.getDescriptor();
        assertThat(descriptor.getDisplayName(), is("Ignore Temp Offline Nodes"));
    }

    @Test
    void testAll() {
        DescriptorExtensionList<NodeEligibility, NodeEligibilityDescriptor> descriptors = NodeEligibility.all();
        assertThat(descriptors, hasItem(hasProperty("displayName", is("All Nodes"))));
        assertThat(descriptors, hasItem(hasProperty("displayName", is("Ignore Offline Nodes"))));
        assertThat(descriptors, hasItem(hasProperty("displayName", is("Ignore Temp Offline Nodes"))));
        assertThat(descriptors.size(), is(3));
    }
}

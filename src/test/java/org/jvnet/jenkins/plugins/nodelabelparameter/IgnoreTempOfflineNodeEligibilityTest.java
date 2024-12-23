package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import hudson.DescriptorExtensionList;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.IgnoreTempOfflineNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.NodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.NodeEligibility.NodeEligibilityDescriptor;

public class IgnoreTempOfflineNodeEligibilityTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    private DumbSlave onlineNode1;

    @Before
    public void setUp() throws Exception {
        onlineNode1 = j.createOnlineSlave(new LabelAtom("label"));
    }

    IgnoreTempOfflineNodeEligibility ignoreTempOfflineNodeEligibility = new IgnoreTempOfflineNodeEligibility();

    @Test
    public void testGetComputer() throws Exception {
        Node node = null;
        // Node is null
        assertFalse(ignoreTempOfflineNodeEligibility.isEligible(node));
        // Node is not null
        assertFalse(ignoreTempOfflineNodeEligibility.isEligible(onlineNode1.getLabelString()));
        // Node is null and nodeName is empty
        assertTrue(ignoreTempOfflineNodeEligibility.isEligible(""));
    }

    @Test
    public void testGetDescriptor() {
        NodeEligibilityDescriptor descriptor = ignoreTempOfflineNodeEligibility.getDescriptor();
        // Check if descriptor is not null
        assertNotNull(descriptor);
        // Check if descriptor is an instance of NodeEligibilityDescriptor
        assertTrue(descriptor instanceof NodeEligibilityDescriptor);
    }

    @Test
    public void testAll() {
        DescriptorExtensionList<NodeEligibility, NodeEligibilityDescriptor> descriptors = NodeEligibility.all();
        // Check if descriptors is not empty
        assertTrue(!descriptors.isEmpty());
        // Check if descriptors is not null
        assertNotNull(descriptors);
    }
}

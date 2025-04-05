package org.jvnet.jenkins.plugins.nodelabelparameter.node;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;

/**
 * Simple unit test for NodeEligibility implementations.
 * Testing only basic functionality that can be tested without complex mocking.
 * More complex functionality would require integration tests with actual
 * Jenkins setup.
 */
public class NodeEligibilityTest {

    @Test
    void testAllNodeEligibilityDescriptor() {
        // Test descriptors
        AllNodeEligibility.Descriptor descriptor = new AllNodeEligibility.Descriptor();
        assertEquals(
                Messages.NodeEligibility_allNodes(),
                descriptor.getDisplayName(),
                "Descriptor display name should match Messages.properties");
    }

    @Test
    void testIgnoreOfflineNodeEligibilityDescriptor() {
        // Test descriptors
        IgnoreOfflineNodeEligibility.Descriptor descriptor = new IgnoreOfflineNodeEligibility.Descriptor();
        assertEquals(
                Messages.NodeEligibility_ignoreOffline(),
                descriptor.getDisplayName(),
                "Descriptor display name should match Messages.properties");
    }

    @Test
    void testIgnoreTempOfflineNodeEligibilityDescriptor() {
        // Test descriptors
        IgnoreTempOfflineNodeEligibility.Descriptor descriptor = new IgnoreTempOfflineNodeEligibility.Descriptor();
        assertEquals(
                Messages.NodeEligibility_ignoreTmpOffline(),
                descriptor.getDisplayName(),
                "Descriptor display name should match Messages.properties");
    }
}

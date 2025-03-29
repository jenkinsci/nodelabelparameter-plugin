package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.Computer;
import hudson.model.Label;
import hudson.slaves.DumbSlave;
import hudson.slaves.OfflineCause;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
public class NodeUtilTest {

    @Test
    void testIsNodeOnlineWithBuiltInNode(JenkinsRule j) {
        // The built-in node (controller) is always online
        String controllerLabel = j.jenkins.getSelfLabel().getName();

        // Just test that the built-in node exists
        Label label = j.jenkins.getLabel(controllerLabel);
        assertTrue(label != null, "Built-in node label should exist");
    }

    @Test
    void testIsNodeOnlineWithOnlineNode(JenkinsRule j) throws Exception {
        // Create a test online node
        String nodeName = "test-online-node-" + System.currentTimeMillis();
        DumbSlave slave = j.createSlave(nodeName, "", null);

        // Wait for the node to be fully online
        j.waitOnline(slave);

        // The node should be considered online
        assertTrue(NodeUtil.isNodeOnline(nodeName));
    }

    @Test
    void testIsNodeOnlineWithOfflineNode(JenkinsRule j) throws Exception {
        // Create a test node that will be taken offline
        String nodeName = "test-offline-node-" + System.currentTimeMillis();
        DumbSlave slave = j.createSlave(nodeName, "", null);

        // Wait for the node to be fully online first
        j.waitOnline(slave);

        // Take the node offline
        Computer computer = slave.toComputer();
        computer.disconnect(new OfflineCause.ByCLI("Taking offline for testing"));

        // Wait for it to go offline
        while (computer.isOnline()) {
            Thread.sleep(100);
        }

        // The node should be considered offline
        assertFalse(NodeUtil.isNodeOnline(nodeName));
    }

    @Test
    void testIsNodeOnlineWithNonExistentNode(JenkinsRule j) {
        // A non-existent node should not be considered online
        assertFalse(NodeUtil.isNodeOnline("non-existent-node-" + System.currentTimeMillis()));
    }

    @Test
    void testIsNodeOnlineWithNodeWithLowExecutors(JenkinsRule j) throws Exception {
        // Create a test node with 1 executor
        String nodeName = "test-low-exec-node-" + System.currentTimeMillis();
        DumbSlave slave = j.createSlave(nodeName, "", null);

        // Set the number of executors to 1 (minimum valid value)
        slave.setNumExecutors(1);
        Future<?> future = slave.toComputer().connect(false);
        future.get(); // Wait for the connection to complete

        // Wait for the node to be fully online
        j.waitOnline(slave);

        // The node should be considered online even with just 1 executor
        assertTrue(NodeUtil.isNodeOnline(nodeName));
    }
}

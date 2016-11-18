package org.jvnet.jenkins.plugins.nodelabelparameter.node;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Node;
import jenkins.model.Jenkins;

import org.jvnet.jenkins.plugins.nodelabelparameter.Constants;

import java.io.Serializable;

public abstract class NodeEligibility implements Describable<NodeEligibility>, ExtensionPoint, Serializable {

    public abstract boolean isEligible(Node node);

    public boolean isEligible(String nodeName) {

        Node node = Jenkins.getActiveInstance().getNode(nodeName);
        if (node == null && (Constants.MASTER.equals(nodeName) || "".equals(nodeName))) {
            Computer c = Jenkins.getActiveInstance().getComputer("");
            node = c != null ? c.getNode() : null;
        }

        return isEligible(node);
    }

    protected Computer getComputer(Node node) {
        String name = Constants.MASTER.equals(node.getNodeName()) ? "" : node.getNodeName();
        return Jenkins.getActiveInstance().getComputer(name);
    }

    protected boolean hasOnlineExecutors(Node node) {
        final Computer c = getComputer(node);
        return c != null && c.isOnline() && c.getNumExecutors() > 0;
    }

    public NodeEligibilityDescriptor getDescriptor() {
        return (NodeEligibilityDescriptor) Jenkins.getActiveInstance().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<NodeEligibility, NodeEligibilityDescriptor> all() {
        return Jenkins.getActiveInstance().<NodeEligibility, NodeEligibilityDescriptor> getDescriptorList(NodeEligibility.class);
    }

    public static abstract class NodeEligibilityDescriptor extends Descriptor<NodeEligibility> {
    }
}

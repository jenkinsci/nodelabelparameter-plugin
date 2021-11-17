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

        String controllerLabel = Jenkins.get().getSelfLabel().getName();
        Node node = Jenkins.get().getNode(nodeName);
        if (node == null && (controllerLabel.equals(nodeName) || "".equals(nodeName))) {
            Computer c = Jenkins.get().getComputer("");
            node = c != null ? c.getNode() : null;
        }

        return isEligible(node);
    }

    protected Computer getComputer(Node node) {
        String controllerLabel = Jenkins.get().getSelfLabel().getName();
        String name = controllerLabel.equals(node.getNodeName()) ? "" : node.getNodeName();
        return Jenkins.get().getComputer(name);
    }

    protected boolean hasOnlineExecutors(Node node) {
        final Computer c = getComputer(node);
        return c != null && c.isOnline() && c.getNumExecutors() > 0;
    }

    public NodeEligibilityDescriptor getDescriptor() {
        return (NodeEligibilityDescriptor) Jenkins.get().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<NodeEligibility, NodeEligibilityDescriptor> all() {
        return Jenkins.get().<NodeEligibility, NodeEligibilityDescriptor> getDescriptorList(NodeEligibility.class);
    }

    public static abstract class NodeEligibilityDescriptor extends Descriptor<NodeEligibility> {
    }
}

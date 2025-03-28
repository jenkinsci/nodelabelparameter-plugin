package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.Computer;
import hudson.model.Node;
import jenkins.model.Jenkins;

public final class NodeUtil {

    private NodeUtil() {}

    /**
     * Checks whether the given node is available for an execution of the job,
     *
     * @param nodeName the name of the node to check
     * @return <code>true</code> if the job is ok to be used
     */
    public static boolean isNodeOnline(String nodeName) {
        return isNodeOnline(nodeName, Jenkins.get());
    }

    /**
     * Checks whether the given node is available for an execution of the job,
     *
     * @param nodeName the name of the node to check
     * @param jenkins  the Jenkins instance to use
     * @return <code>true</code> if the job is ok to be used
     */
    public static boolean isNodeOnline(String nodeName, Jenkins jenkins) {
        String controllerLabel = jenkins.getSelfLabel().getName();
        if (controllerLabel.equals(nodeName)) {
            return true;
        }

        final Computer c = jenkins.getComputer(nodeName);
        if (c != null) {
            Node n = c.getNode();
            // really check if the node is available for execution
            return n != null && c.isOnline() && c.getNumExecutors() > 0;
        }
        return false;
    }
}

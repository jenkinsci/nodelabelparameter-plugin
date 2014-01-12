package org.jvnet.jenkins.plugins.nodelabelparameter.node;

import hudson.Extension;
import hudson.model.Node;

import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * NodeEligibility skipping ALL offline nodes
 * 
 * @author Dominik Bartholdi (imod)
 */
public class IgnoreOfflineNodeEligibility extends NodeEligibility {

    @DataBoundConstructor
    public IgnoreOfflineNodeEligibility() {
    }

    @Override
    public boolean isEligible(Node node) {
        return node != null && hasOnlineExecutors(node);
    }

    @Extension
    public static class Descriptor extends NodeEligibilityDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.NodeEligibility_ignoreOffline();
        }
    }

}

package org.jvnet.jenkins.plugins.nodelabelparameter.node;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Node;

import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * NodeEligibility only skipping temporarly offline node - these nodes are usually taken manually taken offline by a user.
 * 
 * @author Dominik Bartholdi (imod)
 */
public class IgnoreTempOfflineNodeEligibility extends NodeEligibility {

    @DataBoundConstructor
    public IgnoreTempOfflineNodeEligibility() {
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isEligible(Node node) {
        if(node != null){
            final Computer c = getComputer(node);
            if (c != null) {
                boolean isonline = !c.isTemporarilyOffline();
                return isonline && c.getNumExecutors() > 0;
            }
        }
        return false;
    }

    @Extension
    public static class Descriptor extends NodeEligibilityDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.NodeEligibility_ignoreTmpOffline();
        }
    }

}

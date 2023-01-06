package org.jvnet.jenkins.plugins.nodelabelparameter.node;

import hudson.Extension;
import hudson.model.Node;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * NodeEligibility not performing any restrictions
 *
 * @author Dominik Bartholdi (imod)
 */
public class AllNodeEligibility extends NodeEligibility {

    @DataBoundConstructor
    public AllNodeEligibility() {}

    @Override
    public boolean isEligible(Node node) {
        return true;
    }

    @Extension
    public static class Descriptor extends NodeEligibilityDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.NodeEligibility_allNodes();
        }
    }
}

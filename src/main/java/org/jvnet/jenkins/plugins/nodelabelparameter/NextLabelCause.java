package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.Cause.UpstreamCause;
import hudson.model.Run;

/**
 * @author domi
 */
public class NextLabelCause extends UpstreamCause {

    private String label;

    public NextLabelCause(String label, Run<?, ?> up) {
        super(up);
        this.label = label;
    }

    @Override
    public String getShortDescription() {
        return org.jvnet.jenkins.plugins.nodelabelparameter.Messages.NextLabelCause_description(label);
    }
}

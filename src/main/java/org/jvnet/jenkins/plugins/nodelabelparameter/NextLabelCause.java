package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.Cause.UpstreamCause;
import hudson.model.Run;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        NextLabelCause that = (NextLabelCause) o;

        return Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result;
        if (label != null) {
            result = result + label.hashCode();
        }
        return result;
    }
}

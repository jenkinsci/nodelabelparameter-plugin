package org.jvnet.jenkins.plugins.nodelabelparameter.wrapper;

import hudson.model.Cause;

/**
 * @author domi
 */
public class NextLabelCause extends Cause {

	private String label;

	public NextLabelCause(String label) {
		this.label = label;
	}

	@Override
	public String getShortDescription() {
		return org.jvnet.jenkins.plugins.nodelabelparameter.Messages.NextLabelCause_description(label);
	}
}

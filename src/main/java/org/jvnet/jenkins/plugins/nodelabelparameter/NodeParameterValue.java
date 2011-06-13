/**
 * 
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author domi
 * 
 */
public class NodeParameterValue extends LabelParameterValue {

	private static final long serialVersionUID = 1L;

	@DataBoundConstructor
	public NodeParameterValue(String name, String label) {
		super(name, label);
	}

	public NodeParameterValue(String name, String description, String label) {
		super(name, description, label);
	}

	@Override
	public String toString() {
		return "[NodeParameterValue: " + name + "=" + label + "]";
	}
}

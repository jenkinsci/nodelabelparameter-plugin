/**
 * 
 */
package org.jvnet.jenkins.plugins;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import hudson.model.AbstractBuild;
import hudson.model.Label;
import hudson.model.ParameterValue;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.SubTask;
import hudson.util.VariableResolver;

/**
 * @author domi
 * 
 */
public class LabelParameterValue extends ParameterValue {

	@Exported(visibility = 3)
	public final String label;

	/**
	 * @param name
	 */
	@DataBoundConstructor
	public LabelParameterValue(String name, String label) {
		super(name);
		this.label = label;
	}

	/**
	 * @param name
	 * @param description
	 */
	public LabelParameterValue(String name, String description, String label) {
		super(name, description);
		this.label = label;
	}

	@Override
	public Label getAssignedLabel(SubTask task) {
		return new LabelAtom(label);
	}

	@Override
	public VariableResolver<String> createVariableResolver(
			AbstractBuild<?, ?> build) {
		return new VariableResolver<String>() {
			public String resolve(String name) {
				return LabelParameterValue.this.name.equals(name) ? label
						: null;
			}
		};
	}

	@Override
	public String toString() {
		return "[LabelParameterValue: " + name + "=" + label + "]";
	}

}

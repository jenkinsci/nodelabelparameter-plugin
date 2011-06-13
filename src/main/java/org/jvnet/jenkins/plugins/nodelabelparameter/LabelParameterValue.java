/**
 * 
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import hudson.model.AbstractBuild;
import hudson.model.Label;
import hudson.model.ParameterValue;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.SubTask;
import hudson.util.VariableResolver;

/**
 * Defines a build parameter used to select the node where a job should be
 * executed on. Although it is possible to define the node name in the UI at
 * "restrict where this job should run", but that would tide a job to a fix
 * node. This parameter actually allows to define a list of possible nodes and
 * ask the user before execution.
 * 
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

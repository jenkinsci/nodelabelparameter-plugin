/**
 * 
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.AbstractBuild;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.tasks.BuildWrapper;

import java.util.ArrayList;
import java.util.List;

import org.jvnet.jenkins.plugins.nodelabelparameter.wrapper.TriggerNextBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author domi
 * 
 */
public class NodeParameterValue extends LabelParameterValue {

	private static final long serialVersionUID = 1L;
	private List<String> nextLabels;

	/**
	 * creates a new node parameter
	 * 
	 * @param name
	 *            the name of the parameter
	 * @param labels
	 *            the node labels to trigger one build after the other with
	 */
	@DataBoundConstructor
	public NodeParameterValue(String name, List<String> labels) {
		super(name);
		if (labels != null && !labels.isEmpty()) {
			this.setLabel(labels.get(0));
			if (labels.size() > 1) {
				final List<String> subList = labels.subList(1, labels.size());
				nextLabels = new ArrayList<String>();
				for (String l : subList) {
					nextLabels.add(l);
				}
			}
		} else {
			throw new IllegalArgumentException("at least one label must be given!");
		}
	}

	public NodeParameterValue(String name, String description, String label) {
		super(name, description, label);
	}

	@Override
	public String toString() {
		return "[NodeParameterValue: " + name + "=" + getLabel() + ", nextNodes=" + this.nextLabels + "]";
	}

	/**
	 * @return the labels
	 */
	public List<String> getNextLabels() {
		return nextLabels;
	}

	/**
	 * @see hudson.model.ParameterValue#createBuildWrapper(hudson.model.AbstractBuild)
	 */
	@Override
	public BuildWrapper createBuildWrapper(AbstractBuild<?, ?> build) {

		// add a badge icon to the build
		build.addAction(new LabelBadgeAction(getLabel(), "node: " + getLabel()));

		final ParametersDefinitionProperty property = build.getProject().getProperty(hudson.model.ParametersDefinitionProperty.class);
		final List<ParameterDefinition> parameterDefinitions = property.getParameterDefinitions();
		for (ParameterDefinition paramDef : parameterDefinitions) {
			if (paramDef instanceof NodeParameterDefinition) {
				final NodeParameterDefinition nodeParameterDefinition = (NodeParameterDefinition) paramDef;
				if (nodeParameterDefinition.getAllowMultiNodeSelection()) {
					final String triggerIfResult = nodeParameterDefinition.getTriggerIfResult();
					return new TriggerNextBuildWrapper(triggerIfResult);
				} else {
					return null;
				}
			}
		}
		return null;
	}

}

/**
 *
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.AbstractBuild;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.tasks.BuildWrapper;
import org.jvnet.jenkins.plugins.nodelabelparameter.wrapper.TriggerNextBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.List;

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
			this.setLabel(labels.get(0).trim());
			if (labels.size() > 1) {
				final List<String> subList = labels.subList(1, labels.size());
				nextLabels = new ArrayList<String>();
				for (String l : subList) {
					nextLabels.add(l.trim());
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
		StringBuilder s = new StringBuilder("[NodeParameterValue: ");
		s.append(name).append("=").append(getLabel());
		if (nextLabels != null && !nextLabels.isEmpty()) {
			s.append(", nextNodes=").append(this.nextLabels);
		}
		s.append("]");
		return s.toString();
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
		build.addAction(new LabelBadgeAction(getLabel(), Messages.LabelBadgeAction_node_tooltip(getLabel())));

		final ParametersDefinitionProperty property = build.getProject().getProperty(hudson.model.ParametersDefinitionProperty.class);
		final List<ParameterDefinition> parameterDefinitions = property.getParameterDefinitions();
		for (ParameterDefinition paramDef : parameterDefinitions) {
			if (paramDef instanceof NodeParameterDefinition) {
				final NodeParameterDefinition nodeParameterDefinition = (NodeParameterDefinition) paramDef;
				if (nodeParameterDefinition.getAllowMultiNodeSelection()) {
					// we expect only one node parameter definition per job
					return new TriggerNextBuildWrapper(nodeParameterDefinition);
				} else {
					return null;
				}
			}
		}
		return null;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NodeParameterValue that = (NodeParameterValue) o;

        if (nextLabels != null ? !nextLabels.equals(that.nextLabels) : that.nextLabels != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (nextLabels != null ? nextLabels.hashCode() : 0);
        return result;
    }
}

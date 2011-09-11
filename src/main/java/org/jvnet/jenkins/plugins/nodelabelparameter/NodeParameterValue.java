/**
 * 
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.AbstractBuild;
import hudson.tasks.BuildWrapper;

import java.util.List;

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
				this.nextLabels = labels.subList(1, labels.size());
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
	
	
	@Override
	public BuildWrapper createBuildWrapper(AbstractBuild<?, ?> build) {
		return super.createBuildWrapper(build);
	}

}

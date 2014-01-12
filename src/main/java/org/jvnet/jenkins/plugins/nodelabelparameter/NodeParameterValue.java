/**
 *
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.NodeEligibility;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Dominik Bartholdi (imod)
 */
public class NodeParameterValue extends LabelParameterValue {
    
	private static final long serialVersionUID = 1L;

	/**
	 * creates a new node parameter
	 *
	 * @param name
	 *            the name of the parameter
	 * @param labels
	 *            the node labels to trigger one build after the other with
	 * @param nodeEligibility
	 *            defines if a node should be ignored at execution or not.             
	 */
	@DataBoundConstructor
	public NodeParameterValue(String name, List<String> labels, NodeEligibility nodeEligibility) {
		super(name, labels, nodeEligibility);
	}
	
	public NodeParameterValue(String name, String description, String label) {
		super(name, description, label);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("[NodeParameterValue: ");
		s.append(name).append("=").append(getLabel());
		if (nextLabels != null && !nextLabels.isEmpty()) {
			s.append(", nextNodes=").append(StringUtils.join(nextLabels, ','));
		}
		s.append("]");
		return s.toString();
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

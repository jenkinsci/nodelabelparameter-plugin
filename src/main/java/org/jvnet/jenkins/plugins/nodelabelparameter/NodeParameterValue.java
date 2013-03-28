/**
 *
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.tasks.BuildWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.jvnet.jenkins.plugins.nodelabelparameter.wrapper.TriggerNextBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Dominik Bartholdi (imod)
 */
public class NodeParameterValue extends LabelParameterValue {
    
    private static final Logger LOGGER = Logger.getLogger(NodeParameterValue.class.getName());

	private static final long serialVersionUID = 1L;
	private List<String> nextLabels;

	/**
	 * creates a new node parameter
	 *
	 * @param name
	 *            the name of the parameter
	 * @param labels
	 *            the node labels to trigger one build after the other with
	 * @param ignoreOfflineNodes
	 *            if the job should also be triggered on nodes which currently are not available for execution.             
	 */
	@DataBoundConstructor
	public NodeParameterValue(String name, List<String> labels, boolean ignoreOfflineNodes) {
		super(name);
		if (labels != null && !labels.isEmpty()) {
		    
		    nextLabels = new ArrayList<String>();
		    if(ignoreOfflineNodes){
    		    for (String nodeName : labels) {
    		        nodeName = nodeName.trim();
    		        if(NodeUtil.isNodeOnline(nodeName)) {
                        if(getLabel() == null){
    		                this.setLabel(nodeName);
    		            } else {
    		                nextLabels.add(nodeName);
    		            }
    		        } else {
    		            LOGGER.fine("Skipping execution on offline node ["+nodeName+"]");
    		        }
                }
		    } else {
		        this.setLabel(labels.get(0).trim());
    			if (labels.size() > 1) {
    				final List<String> subList = labels.subList(1, labels.size());
    				for (String l : subList) {
    					nextLabels.add(l.trim());
    				}
    			}
		    }
		} 
		if(getLabel() == null || getLabel().length() == 0){
		    // these artificial labels will cause the job to stay in the queue and the user will see this label
		    if (ignoreOfflineNodes) {
		        setLabel("Job triggered without a valid online node, given where: "+StringUtils.join(labels, ','));
		    } else {
		        setLabel("Job triggered, but no node given");
		    }
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
			s.append(", nextNodes=").append(StringUtils.join(nextLabels, ','));
		}
		s.append("]");
		return s.toString();
	}

	/**
	 * Gets the labels to be used to trigger the next builds with
	 * 
	 * @return the labels
	 */
	public List<String> getNextLabels() {
		return Collections.unmodifiableList(nextLabels == null ? new ArrayList<String>() : nextLabels);
	}
	

	/**
	 * @see hudson.model.ParameterValue#createBuildWrapper(hudson.model.AbstractBuild)
	 */
	@Override
	public BuildWrapper createBuildWrapper(AbstractBuild<?, ?> build) {

		// add a badge icon to the build
		build.addAction(new LabelBadgeAction(getLabel(), Messages.LabelBadgeAction_node_tooltip(getLabel())));

		final ParametersDefinitionProperty property = build.getProject().getProperty(hudson.model.ParametersDefinitionProperty.class);
        if (property != null) {
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

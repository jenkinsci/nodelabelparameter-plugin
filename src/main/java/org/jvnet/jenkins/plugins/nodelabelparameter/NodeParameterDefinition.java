/**
 *
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.Node;
import hudson.model.ParameterDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.jvnet.jenkins.plugins.nodelabelparameter.wrapper.TriggerNextBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Defines a build parameter used to select the node where a job should be
 * executed on. Although it is possible to define the node name in the UI at
 * "restrict where this job should run", but that would tide a job to a fix
 * node. This parameter actually allows to define a list of possible nodes and
 * ask the user before execution.
 *
 * @author Dominik Bartholdi (imod)
 *
 */
public class NodeParameterDefinition extends SimpleParameterDefinition implements MultipleNodeDescribingParameterDefinition {

	private static final long serialVersionUID = 1L;

	public final List<String> allowedSlaves;
	private List<String> defaultSlaves;
	@Deprecated
	public transient String defaultValue;
	private String triggerIfResult;
	private boolean allowMultiNodeSelection;
	private boolean triggerConcurrentBuilds;
	private boolean ignoreOfflineNodes;

    @DataBoundConstructor
    public NodeParameterDefinition(String name, String description, List<String> defaultSlaves, List<String> allowedSlaves, String triggerIfResult, boolean ignoreOfflineNodes) {
        super(name, description);
        this.allowedSlaves = allowedSlaves;
        this.defaultSlaves = defaultSlaves;

        if (Constants.CASE_MULTISELECT_DISALLOWED.equals(triggerIfResult)) {
            this.allowMultiNodeSelection = false;
            this.triggerConcurrentBuilds = false;
        } else if (Constants.CASE_MULTISELECT_CONCURRENT_BUILDS.equals(triggerIfResult)) {
            this.allowMultiNodeSelection = true;
            this.triggerConcurrentBuilds = true;
        } else {
            this.allowMultiNodeSelection = true;
            this.triggerConcurrentBuilds = false;
        }
        this.triggerIfResult = triggerIfResult;
        this.ignoreOfflineNodes = ignoreOfflineNodes;
    }
    
    @Deprecated
	public NodeParameterDefinition(String name, String description, String defaultValue, List<String> allowedSlaves, String triggerIfResult) {
		super(name, description);
		this.allowedSlaves = allowedSlaves;

		if (this.allowedSlaves != null && this.allowedSlaves.contains(defaultValue)) {
			this.allowedSlaves.remove(defaultValue);
			this.allowedSlaves.add(0, defaultValue);
		}

		if (Constants.CASE_MULTISELECT_DISALLOWED.equals(triggerIfResult)) {
			this.allowMultiNodeSelection = false;
			this.triggerConcurrentBuilds = false;
		} else if (Constants.CASE_MULTISELECT_CONCURRENT_BUILDS.equals(triggerIfResult)) {
			this.allowMultiNodeSelection = true;
			this.triggerConcurrentBuilds = true;
		} else {
			this.allowMultiNodeSelection = true;
			this.triggerConcurrentBuilds = false;
		}
		this.triggerIfResult = triggerIfResult;
		this.ignoreOfflineNodes = false;
	}
	
	public List<String> getDefaultSlaves() {
        return defaultSlaves;
    }
	
	public boolean isIgnoreOfflineNodes() {
        return ignoreOfflineNodes;
    }

	/**
	 * e.g. what to show if a build is triggered by hand?
	 */
	@Override
	public NodeParameterValue getDefaultParameterValue() {
		return new NodeParameterValue(getName(), getDefaultSlaves(), isIgnoreOfflineNodes());
	}

	@Override
	public ParameterValue createValue(String value) {
		return new NodeParameterValue(getName(), getDescription(), value);
	}

	@Override
	public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValueObj) {
		return this;
	}

	/**
	 * Returns a list of nodes the job could run on. If allowed nodes is empty,
	 * it falls back to all nodes
	 *
	 * @return list of nodenames.
	 */
	public List<String> getAllowedNodesOrAll() {
		final List<String> slaves = allowedSlaves == null || allowedSlaves.isEmpty() || allowedSlaves.contains(Constants.ALL_NODES) ? getNodeNames() : allowedSlaves;

		Collections.sort(slaves, NodeNameComparator.INSTANCE);
		if(slaves.contains(Constants.MASTER)) {
		    moveMasterToFirstPossition(slaves);
		}

		return slaves;
	}

	/**
	 * @return the triggerIfResult
	 */
	public String getTriggerIfResult() {
		return triggerIfResult;
	}

	/**
	 * returns all available nodes plus an identifier to identify all slaves at
	 * position one.
	 *
	 * @return list of node names
	 */
	public static List<String> getSlaveNamesForSelection() {
		List<String> slaveNames = getNodeNames();
		slaveNames.add(0, Constants.ALL_NODES);
		return slaveNames;
	}

	/**
	 * Gets the names of all configured slaves, regardless whether they are
	 * online.
	 *
	 * @return list with all slave names
	 */
	public static List<String> getSlaveNames() {
		return getNodeNames();
	}
	
	/**
	 * Gets all node names - sorted and 'master' at first position.
	 *  
	 * @return a list of all node names. 
	 */
	private static List<String> getNodeNames(){
	    List<String> names = new ArrayList<String>();
	    final List<Node> nodes = Jenkins.getInstance().getNodes();
	    for (Node node : nodes) {
            final String nodeName = node.getNodeName();
            if(StringUtils.isNotBlank(nodeName)) {
                names.add(nodeName);
            }
        }
	    Collections.sort(names, NodeNameComparator.INSTANCE);
	    
        // add 'magic' name for master, so all nodes can be handled the same way
        moveMasterToFirstPossition(names);	    
	    return names;
	}
	
	private static void moveMasterToFirstPossition(List<String> nodeList) {
	    nodeList.remove(Constants.MASTER);
	    nodeList.add(0, Constants.MASTER);
	}
	
	/**
	 * Comparator preferring the master name
	 */
	private static final class NodeNameComparator implements Comparator<String> {
	    public static final NodeNameComparator INSTANCE = new NodeNameComparator();
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

	public void validateBuild(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        if (build.getProject().isConcurrentBuild() && !this.isTriggerConcurrentBuilds()) {
            final String msg = Messages.BuildWrapper_param_not_concurrent(this.getName());
            throw new IllegalStateException(msg);
        } else if (!build.getProject().isConcurrentBuild() && this.isTriggerConcurrentBuilds()) { 
            final String msg = Messages.BuildWrapper_project_not_concurrent(this.getName());
            throw new IllegalStateException(msg);
        }
	}
	
    @Extension
	public static class DescriptorImpl extends ParameterDescriptor {
		@Override
		public String getDisplayName() {
			return "Node";
		}

		@Override
		public String getHelpFile() {
			return "/plugin/nodelabelparameter/nodeparam.html";
		}
	}

	@Override
	public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
		// as String from UI: {"labels":"master","name":"HOSTN"}
		// as JSONArray: {"name":"HOSTN","value":["master","host2"]}
		// as String from script: {"name":"HOSTN","value":"master"}
		final String name = jo.getString("name");
		final Object joValue = jo.get("value") == null ? jo.get("labels") : jo.get("value");

		List<String> nodes = new ArrayList<String>();
		if (joValue instanceof String) {
			nodes.add((String) joValue);
		} else if (joValue instanceof JSONArray) {
			JSONArray ja = (JSONArray) joValue;
			for (Object strObj : ja) {
				nodes.add((String) strObj);
			}
		}

		NodeParameterValue value = new NodeParameterValue(name, nodes, isIgnoreOfflineNodes());
		value.setDescription(getDescription());
		return value;
	}

	/**
	 * @return the allowMultiNodeSelection
	 */
	public boolean getAllowMultiNodeSelection() {
		return allowMultiNodeSelection;
	}

	/**
	 * @return the triggerConcurrentBuilds
	 */
	public boolean isTriggerConcurrentBuilds() {
		return triggerConcurrentBuilds;
	}
	
    /*
     * keep backward compatible
     */
    public Object readResolve() {
        if (defaultValue != null) {
            if (defaultSlaves == null) {
                defaultSlaves = new ArrayList<String>();
            }
            defaultSlaves.add(defaultValue);
        }
        return this;
    }	
    
    public TriggerNextBuildWrapper createBuildWrapper() {
        if (this.getAllowMultiNodeSelection()) {
            // we expect only one node parameter definition per job
            return new TriggerNextBuildWrapper(this);
        }
        return null;
    }

}

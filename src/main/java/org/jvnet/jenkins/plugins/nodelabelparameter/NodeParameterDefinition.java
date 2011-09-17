/**
 * 
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.ComputerSet;
import hudson.model.Hudson;
import hudson.model.ParameterDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

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
public class NodeParameterDefinition extends SimpleParameterDefinition {

	private static final long serialVersionUID = 1L;

	public static final String ALL_NODES = "ALL (no restriction)";

	public final List<String> allowedSlaves;
	public final String defaultValue;
	private String triggerIfResult;
	private boolean allowMultiNodeSelection;
	private boolean triggerConcurrentBuilds;

	@DataBoundConstructor
	public NodeParameterDefinition(String name, String description, String defaultValue, List<String> allowedSlaves, String triggerIfResult) {
		super(name, description);
		this.defaultValue = defaultValue;
		this.allowedSlaves = allowedSlaves;

		if (this.allowedSlaves != null && this.allowedSlaves.contains(defaultValue)) {
			this.allowedSlaves.remove(defaultValue);
			this.allowedSlaves.add(0, defaultValue);
		}

		if ("multiSelectionDisallowed".equals(triggerIfResult)) {
			this.allowMultiNodeSelection = false;
			this.triggerConcurrentBuilds = false;
		} else if ("allowMultiSelectionForConcurrentBuilds".equals(triggerIfResult)) {
			this.allowMultiNodeSelection = true;
			this.triggerConcurrentBuilds = true;
		} else {
			this.allowMultiNodeSelection = true;
			this.triggerConcurrentBuilds = false;
		}
		this.triggerIfResult = triggerIfResult;
	}

	/**
	 * e.g. what to show if a build is triggered by hand?
	 */
	@Override
	public NodeParameterValue getDefaultParameterValue() {
		NodeParameterValue v = new NodeParameterValue(getName(), getDescription(), defaultValue);
		return v;
	}

	@Override
	public ParameterValue createValue(String value) {
		return new NodeParameterValue(getName(), value, getDescription());
	}

	@Override
	public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValueObj) {
		if (defaultValueObj instanceof NodeParameterValue) {
			NodeParameterValue value = (NodeParameterValue) defaultValueObj;
			return new NodeParameterDefinition(getName(), getDescription(), value.getLabel(), getSlaveNames(), triggerIfResult);
		} else {
			return this;
		}
	}

	/**
	 * Returns a list of nodes the job could run on. If allowed nodes is empty,
	 * it falls back to all nodes
	 * 
	 * @return list of nodenames.
	 */
	public List<String> getAllowedNodesOrAll() {
		final List<String> slaves = allowedSlaves == null || allowedSlaves.isEmpty() || allowedSlaves.contains(ALL_NODES) ? getSlaveNames() : allowedSlaves;
		
		Collections.sort(slaves, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return defaultValue.compareTo(o2);
			}
		});
		
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
		List<String> slaveNames = getSlaveNames();
		slaveNames.add(0, ALL_NODES);
		return slaveNames;
	}

	/**
	 * Gets the names of all configured slaves, regardless whether they are
	 * online.
	 * 
	 * @return list with all slave names
	 */
	@SuppressWarnings("deprecation")
	public static List<String> getSlaveNames() {
		ComputerSet computers = Hudson.getInstance().getComputer();
		List<String> slaveNames = computers.get_slaveNames();

		// slaveNames is unmodifiable, therefore create a new list
		List<String> test = new ArrayList<String>();
		test.addAll(slaveNames);

		// add 'magic' name for master, so all nodes can be handled the same way
		if (!test.contains("master")) {
			test.add("master");
		}
		return test;
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

		NodeParameterValue value = new NodeParameterValue(name, nodes);
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

}

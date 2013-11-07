/**
 *
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import antlr.ANTLRException;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.*;
import hudson.model.labels.LabelExpression;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    public final String allowedLabelExpression;
    private List<String> defaultSlaves;
	@Deprecated
	public transient String defaultValue;
	private String triggerIfResult;
	private boolean allowMultiNodeSelection;
	private boolean triggerConcurrentBuilds;
	private boolean ignoreOfflineNodes;

    @DataBoundConstructor
    public NodeParameterDefinition(String name, String description, List<String> defaultSlaves, List<String> allowedSlaves, String allowedLabelExpression, String triggerIfResult, boolean ignoreOfflineNodes) {
        super(name, description);
        this.allowedSlaves = allowedSlaves;
        this.defaultSlaves = defaultSlaves;

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
        this.ignoreOfflineNodes = ignoreOfflineNodes;
        this.allowedLabelExpression = allowedLabelExpression;
    }

    @Deprecated
	public NodeParameterDefinition(String name, String description, String defaultValue, List<String> allowedSlaves, String triggerIfResult) {
		super(name, description);
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
		this.ignoreOfflineNodes = false;
        this.allowedLabelExpression = null;
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
        final List<String> slaves;
        if (!Strings.isNullOrEmpty(allowedLabelExpression)) {
            slaves = getMatchingSlaves();
        } else {
            slaves = allowedSlaves == null || allowedSlaves.isEmpty() || allowedSlaves.contains(ALL_NODES) ? getSlaveNames() : allowedSlaves;
        }

		Collections.sort(slaves, NodeNameComparator.INSTANCE);

		return slaves;
    }

    private List<String> getMatchingSlaves() {
        final Label label;
        try {
            label = LabelExpression.parseExpression(allowedLabelExpression);
        } catch (ANTLRException e) {
            //already checked in form validation, should not happen
            return Collections.emptyList();
        }
        return Lists.newArrayList(
                Collections2.transform(label.getNodes(), new Function<Node, String>() {
                    @Nullable
                    public String apply(@Nullable Node input) {
                        return input instanceof Hudson ? "master" : input.getNodeName();
                    }
                }));
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
		Collections.sort(slaveNames, NodeNameComparator.INSTANCE);
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
			test.add(0, "master");
		}
		return test;
	}

	/**
	 * Comparator preferring the master name
	 */
	private static final class NodeNameComparator implements Comparator<String> {
	    public static final NodeNameComparator INSTANCE = new NodeNameComparator();
        public int compare(String o1, String o2) {
            if("master".endsWith(o1)){
                return -1;
            }
            return o1.compareTo(o2);
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

        /**
         * Autocompletion method, called by UI to
         *
         * @param value
         * @return
         */
        public AutoCompletionCandidates doAutoCompleteAllowedLabelExpression(@QueryParameter String value) {
            final AutoCompletionCandidates candidates = new AutoCompletionCandidates();

            for (Label l : Jenkins.getInstance().getLabels()) {
                String label = l.getExpression();
                if (StringUtils.containsIgnoreCase(label, value)) {
                    candidates.add(label);
                }
            }

            return candidates;

        }

        public FormValidation doCheckAllowedLabelExpression(@QueryParameter String value, @QueryParameter String allowedSlaves) {
            if (value.isEmpty())
                return FormValidation.ok();
            try {
                Label label = LabelExpression.parseExpression(value); //validates expression
                if (!allowedSlaves.isEmpty())
                    return FormValidation.warning(Messages.NodeLabelParameterDefinition_nodeSelectionNotUsed());
                int matchingNodeCount = label.getNodes().size();
                return matchingNodeCount == 0
                        ? FormValidation.warning(Messages.NodeLabelParameterDefinition_noNodeMatched(value))
                        : FormValidation.ok();
            } catch (ANTLRException e) {
                return FormValidation.error(Messages.NodeLabelParameterDefinition_labelExpressionNotValid(value, e.getMessage()));
            }
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

}

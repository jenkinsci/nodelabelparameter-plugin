/**
 *
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import java.util.Collection;
import java.util.Set;

import javax.servlet.ServletException;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AutoCompletionCandidates;
import hudson.model.BuildListener;
import hudson.model.ParameterValue;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.ParameterDefinition;
import hudson.model.labels.LabelExpression;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

import antlr.ANTLRException;

/**
 * Defines a build parameter used to restrict the node a job will be executed
 * on. Such a label works exactly the same way as if you would define it in the
 * UI "restrict where this job should run".
 *
 * @author Dominik Bartholdi (imod)
 *
 */
public class LabelParameterDefinition extends ParameterDefinition implements MultipleNodeDescribingParameterDefinition {

	public final String defaultValue;
	private boolean allNodesMatchingLabel;
	private boolean ignoreOfflineNodes;
	private String triggerIfResult;
	

	@DataBoundConstructor
	public LabelParameterDefinition(String name, String description, String defaultValue, boolean allNodesMatchingLabel, boolean ignoreOfflineNodes, String triggerIfResult) {
		super(name, description);
		this.defaultValue = defaultValue;
		this.allNodesMatchingLabel = allNodesMatchingLabel;
		this.ignoreOfflineNodes = ignoreOfflineNodes;
		this.triggerIfResult = StringUtils.isBlank(triggerIfResult) ? ALL_CASES : triggerIfResult;
	}

	@Deprecated
	public LabelParameterDefinition(String name, String description, String defaultValue) {
        this(name, description, defaultValue, false, false, ALL_CASES);
    }
	
	@Override
	public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValueObj) {
		if (defaultValueObj instanceof LabelParameterValue) {
			LabelParameterValue value = (LabelParameterValue) defaultValueObj;
			return new LabelParameterDefinition(getName(), getDescription(), value.getLabel(), allNodesMatchingLabel, ignoreOfflineNodes, triggerIfResult);
		} else {
			return this;
		}
	}

	@Override
	public LabelParameterValue getDefaultParameterValue() {
		return new LabelParameterValue(getName(), getDescription(), defaultValue);
	}
	
	public boolean isAllNodesMatchingLabel() {
        return allNodesMatchingLabel;
    }
	
	public boolean isIgnoreOfflineNodes() {
        return ignoreOfflineNodes;
    }

	@Extension
	public static class DescriptorImpl extends ParameterDescriptor {
		@Override
		public String getDisplayName() {
			return "Label";
		}

		@Override
		public String getHelpFile() {
			return "/plugin/nodelabelparameter/labelparam.html";
		}
		
        /**
         * Autocompletion method, called by UI to
         *
         * @param value
         * @return
         */
        public AutoCompletionCandidates doAutoCompleteDefaultValue(@QueryParameter String value) {
            final AutoCompletionCandidates candidates = new AutoCompletionCandidates();
            for (Label l : Jenkins.getInstance().getLabels()) {
                String label = l.getExpression();
                if (StringUtils.containsIgnoreCase(label, value)) {
                    candidates.add(label);
                }
            }
            return candidates;
        }

        public FormValidation doCheckDefaultValue(@QueryParameter String value) {
            if (value.isEmpty())
                return FormValidation.ok();
            try {
                int matchingNodeCount = getNodesForLabel(value).size(); //validates expression
                return matchingNodeCount == 0
                        ? FormValidation.warning(Messages.NodeLabelParameterDefinition_noNodeMatched(value))
                                : FormValidation.ok();
            } catch (ANTLRException e) {
                return FormValidation.error(Messages.NodeLabelParameterDefinition_labelExpressionNotValid(value, e.getMessage()));
            }
        }
        
        public FormValidation doListNodesForLabel(@QueryParameter("label") final String label) throws ServletException {

            if (StringUtils.isBlank(label))
                return FormValidation.error("a label is required");
            try {
                final Set<Node> nodes = getNodesForLabel(label);
                if(nodes.isEmpty()) {
                    return FormValidation.warning(Messages.NodeLabelParameterDefinition_noNodeMatched(label));
                }
                final Collection<String> nodeNames = Collections2.transform(nodes, new NodeDescFunction());
                final String html = Joiner.on("</li><li>").join(nodeNames);
                return FormValidation.okWithMarkup("<b>Found nodes:</b> <ul><li>" + html +"</li></ul>");
            } catch (ANTLRException e) {
                return FormValidation.error(Messages.NodeLabelParameterDefinition_labelExpressionNotValid(label, e.getMessage()));
            }
        }        
        
        private Set<Node> getNodesForLabel(String labelExp) throws ANTLRException {
            Label label = LabelExpression.parseExpression(labelExp);
            return label.getNodes();
        }
        
        /**
         * function providing the node description for UI when listing matching nodes
         */
        private static final class NodeDescFunction implements Function<Node, String> {
            public String apply(Node n) {
                final String nodeName = StringUtils.isBlank(n.getNodeName()) ? "master" : n.getNodeName(); 
                return nodeName + (NodeUtil.isNodeOnline(nodeName) ? "" : " (offline)");
            }
        }
	}

	@Override
	public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
		LabelParameterValue value = req.bindJSON(LabelParameterValue.class, jo);
		value.setDescription(getDescription());
		// JENKINS-17660 for convenience, many users use 'value' instead of label - so we make a small hack to allow this too 
		if(StringUtils.isBlank(value.getLabel())) {
		    final String label = jo.optString("value");
		    value.setLabel(label);
		}
		return value;
	}

    @Override
    public final ParameterValue createValue(StaplerRequest req) {
        String[] value = req.getParameterValues(getName());
        if (value == null) {
            return getDefaultParameterValue();
        } else if (value.length != 1) {
            throw new IllegalArgumentException("Illegal number of parameter values for " + getName() + ": " + value.length);
        } 
        return new LabelParameterValue(getName(), value[0], allNodesMatchingLabel, ignoreOfflineNodes);
    }

    public String getTriggerIfResult() {
        return triggerIfResult;
    }

    public boolean isTriggerConcurrentBuilds() {
        return ALL_CASES.equals(triggerIfResult);
    }

    public void validateBuild(AbstractBuild build, Launcher launcher, BuildListener listener) {
        if (build.getProject().isConcurrentBuild() && !this.isTriggerConcurrentBuilds()) {
            final String msg = Messages.BuildWrapper_param_not_concurrent(this.getName());
            throw new IllegalStateException(msg);
        }
    }
	
}

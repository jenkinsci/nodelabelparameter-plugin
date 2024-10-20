/** */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AutoCompletionCandidates;
import hudson.model.BuildListener;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.labels.LabelExpression;
import hudson.util.FormValidation;
import jakarta.servlet.ServletException;
import java.io.Serial;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.IgnoreOfflineNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.NodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.wrapper.TriggerNextBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;

/**
 * Defines a build parameter used to restrict the node a job will be executed on. Such a label works
 * exactly the same way as if you would define it in the UI "restrict where this job should run".
 *
 * @author Dominik Bartholdi (imod)
 */
public class LabelParameterDefinition extends SimpleParameterDefinition
        implements MultipleNodeDescribingParameterDefinition {

    @Serial
    private static final long serialVersionUID = 1L;

    public final String defaultValue;
    private boolean allNodesMatchingLabel;

    @Deprecated
    private transient boolean ignoreOfflineNodes;

    private String triggerIfResult;

    private NodeEligibility nodeEligibility;

    @DataBoundConstructor
    public LabelParameterDefinition(
            String name,
            String description,
            String defaultValue,
            boolean allNodesMatchingLabel,
            NodeEligibility nodeEligibility,
            String triggerIfResult) {
        super(name, description);
        this.defaultValue = defaultValue;
        this.allNodesMatchingLabel = allNodesMatchingLabel;
        this.nodeEligibility = nodeEligibility == null ? new AllNodeEligibility() : nodeEligibility;
        this.triggerIfResult = StringUtils.isBlank(triggerIfResult) ? Constants.ALL_CASES : triggerIfResult;
    }

    @Deprecated
    public LabelParameterDefinition(
            String name,
            String description,
            String defaultValue,
            boolean allNodesMatchingLabel,
            boolean ignoreOfflineNodes,
            String triggerIfResult) {
        super(name, description);
        this.defaultValue = defaultValue;
        this.allNodesMatchingLabel = allNodesMatchingLabel;
        if (ignoreOfflineNodes) {
            this.nodeEligibility = new IgnoreOfflineNodeEligibility();
        } else {
            this.nodeEligibility = new AllNodeEligibility();
        }
        this.triggerIfResult = StringUtils.isBlank(triggerIfResult) ? Constants.ALL_CASES : triggerIfResult;
    }

    @Deprecated
    public LabelParameterDefinition(String name, String description, String defaultValue) {
        this(name, description, defaultValue, false, false, Constants.ALL_CASES);
    }

    @Override
    public SimpleParameterDefinition copyWithDefaultValue(ParameterValue defaultValueObj) {
        if (defaultValueObj instanceof LabelParameterValue value) {
            return new LabelParameterDefinition(
                    getName(),
                    getDescription(),
                    value.getLabel(),
                    allNodesMatchingLabel,
                    ignoreOfflineNodes,
                    triggerIfResult);
        } else {
            return this;
        }
    }

    @Override
    public LabelParameterValue getDefaultParameterValue() {
        return new LabelParameterValue(getName(), defaultValue, allNodesMatchingLabel, nodeEligibility);
    }

    public boolean isAllNodesMatchingLabel() {
        return allNodesMatchingLabel;
    }

    @Override
    public NodeEligibility getNodeEligibility() {
        return nodeEligibility;
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
         * Called by UI - Autocompletion for label values
         *
         * @param value the current value in the text field to base the automcompetion upon.
         * @return autocompletion candidates for label values
         */
        public AutoCompletionCandidates doAutoCompleteDefaultValue(@QueryParameter String value) {
            final AutoCompletionCandidates candidates = new AutoCompletionCandidates();
            for (Label l : Jenkins.get().getLabels()) {
                String label = l.getExpression();
                if (StringUtils.containsIgnoreCase(label, value)) {
                    candidates.add(label);
                }
            }
            return candidates;
        }

        /**
         * Called by UI - Checks whether the given label is valid
         *
         * @param value the label to be checked
         * @return validation result for the form
         */
        public FormValidation doCheckDefaultValue(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.ok();
            }
            try {
                int matchingNodeCount = getNodesForLabel(value).size(); // validates expression
                return matchingNodeCount == 0
                        ? FormValidation.warning(Messages.NodeLabelParameterDefinition_noNodeMatched(value))
                        : FormValidation.ok();
            } catch (IllegalArgumentException e) {
                return FormValidation.error(
                        Messages.NodeLabelParameterDefinition_labelExpressionNotValid(value, e.getMessage()));
            }
        }

        /**
         * Called by validation button in UI when triggering a job manually
         *
         * @param label the label to search the nodes for
         * @return if ok, a list of nodes matching the given label
         * @throws ServletException on error
         */
        public FormValidation doListNodesForLabel(@QueryParameter("value") final String label) throws ServletException {

            if (StringUtils.isBlank(label)) {
                return FormValidation.error(Messages.LabelParameterDefinition_labelRequired());
            }
            try {
                final Set<Node> nodes = getNodesForLabel(label);
                if (nodes.isEmpty()) {
                    return FormValidation.warning(Messages.NodeLabelParameterDefinition_noNodeMatched(label));
                }
                final List<String> nodeNames =
                        nodes.stream().map(new NodeDescFunction()).collect(Collectors.toList());
                final String html = String.join("</li><li>", nodeNames);
                return FormValidation.okWithMarkup("<b>"
                        + Messages.LabelParameterDefinition_matchingNodes()
                        + "</b><ul><li>"
                        + html
                        + "</li></ul>");
            } catch (IllegalArgumentException e) {
                return FormValidation.error(
                        Messages.NodeLabelParameterDefinition_labelExpressionNotValid(label, e.getMessage()));
            }
        }

        private Set<Node> getNodesForLabel(String labelExp) {
            Label label = LabelExpression.parseExpression(labelExp);
            return label.getNodes();
        }

        /** provides the default node eligibility for the UI */
        public NodeEligibility getDefaultNodeEligibility() {
            return new AllNodeEligibility();
        }

        /** function providing the node description for UI when listing matching nodes */
        private static final class NodeDescFunction implements Function<Node, String> {
            @Override
            public String apply(Node n) {
                String controllerLabel = Jenkins.get().getSelfLabel().getName();
                return n != null && StringUtils.isNotBlank(n.getNodeName()) ? n.getNodeName() : controllerLabel;
            }
        }
    }

    @Override
    public ParameterValue createValue(StaplerRequest2 req, JSONObject jo) {
        LabelParameterValue value = req.bindJSON(LabelParameterValue.class, jo);
        value.setDescription(getDescription());

        // JENKINS-17660 for convenience, many users use 'value' instead of label - so we make a
        // small hack to allow this too
        if (StringUtils.isBlank(value.getLabel())) {
            final String label = jo.optString("value");
            value.setLabel(label);
        }
        value.computeNextLabels(allNodesMatchingLabel, nodeEligibility);
        return value;
    }

    @Override
    public ParameterValue createValue(String str) {
        return new LabelParameterValue(getName(), str, allNodesMatchingLabel, nodeEligibility);
    }

    @Override
    public String getTriggerIfResult() {
        return triggerIfResult;
    }

    public boolean isTriggerConcurrentBuilds() {
        return Constants.ALL_CASES.equals(triggerIfResult);
    }

    @Override
    public void validateBuild(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        if (build.getProject().isConcurrentBuild() && !this.isTriggerConcurrentBuilds()) {
            final String msg = Messages.BuildWrapper_param_not_concurrent(this.getName());
            throw new IllegalStateException(msg);
        }
    }

    @Override
    public TriggerNextBuildWrapper createBuildWrapper() {
        if (this.isAllNodesMatchingLabel()) {
            // we expect only one node parameter definition per job
            return new TriggerNextBuildWrapper(this);
        }
        return null;
    }
}

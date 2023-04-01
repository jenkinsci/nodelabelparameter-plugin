/** */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import antlr.ANTLRException;
import hudson.EnvVars;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Run;
import hudson.model.labels.LabelExpression;
import hudson.model.queue.SubTask;
import hudson.tasks.BuildWrapper;
import hudson.util.VariableResolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.NodeEligibility;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

/**
 * @author Dominik Bartholdi (imod)
 */
public class LabelParameterValue extends ParameterValue {

    private static final Logger LOGGER = Logger.getLogger(LabelParameterValue.class.getName());

    private static final String DEFAULT_NAME = "NODELABEL";

    @Exported(visibility = 3)
    private String label;

    protected List<String> nextLabels;

    public LabelParameterValue(String name) {
        super(nameOrDefault(name));
    }

    @Deprecated
    public LabelParameterValue(String name, String label) {
        this(name, label, false, new AllNodeEligibility());
    }

    public LabelParameterValue(String name, List<String> labels, NodeEligibility nodeEligibility) {
        super(name);
        setNextLabels(labels, nodeEligibility);
    }

    /**
     * @param name parameter value
     * @param label parameter label
     * @param allNodesMatchingLabel if true, then all nodes matching the label are to be used
     * @param nodeEligibility node eligibility definition
     */
    @DataBoundConstructor
    public LabelParameterValue(
            String name, String label, boolean allNodesMatchingLabel, NodeEligibility nodeEligibility) {
        super(nameOrDefault(name));
        if (label != null) {
            this.label = label.trim();
        }

        computeNextLabels(allNodesMatchingLabel, nodeEligibility);
    }

    /* package */ void computeNextLabels(boolean allNodesMatchingLabel, NodeEligibility nodeEligibility) {
        if (allNodesMatchingLabel && label != null) {
            List<String> labels = getNodeNamesForLabelExpression(label);
            if (labels.isEmpty()) {
                // we are not able to determine a node for the given label - let Jenkins inform the
                // user about it, by placing the job into the queue
                labels.add(label);
            }
            setNextLabels(labels, nodeEligibility);
        }
    }

    private void setNextLabels(List<String> labels, NodeEligibility nodeEligibility) {
        if (labels != null && !labels.isEmpty()) {

            List<String> tmpLabels = new ArrayList<>(labels);
            nextLabels = new ArrayList<>();

            for (String nodeName : tmpLabels) {
                if (nodeEligibility.isEligible(nodeName)) {
                    if (getLabel() == null && NodeUtil.isNodeOnline(nodeName)) {
                        // search for the first online node we can use, otherwise we might get
                        // needlessly stuck in the queue before we even start the first job
                        this.setLabel(nodeName.trim());
                    } else {
                        nextLabels.add(nodeName);
                    }
                } else {
                    LOGGER.fine(Messages.NodeListBuildParameterFactory_skippOfflineNode(nodeName));
                }
            }

            if (getLabel() == null) {
                // we did not find an online node, therefore we use the first entry in the requested
                // list
                if (!nextLabels.isEmpty()) {
                    this.setLabel(nextLabels.remove(0).trim());
                }
            }
        }
        if (StringUtils.isBlank(getLabel())) {
            // these artificial label will cause the job to stay in the queue and the user will see
            // this label
            setLabel(Messages.LabelParameterValue_triggerWithoutValidOnlineNode(StringUtils.join(labels, ',')));
        }
    }

    private List<String> getNodeNamesForLabelExpression(String labelExp) {
        List<String> nodeNames = new ArrayList<>();
        try {
            Label label = LabelExpression.parseExpression(labelExp);
            for (Node node : label.getNodes()) {
                nodeNames.add(node.getSelfLabel().getName());
            }
        } catch (ANTLRException e) {
            LOGGER.log(Level.SEVERE, "failed to parse label [" + labelExp + "]", e);
        }
        return nodeNames;
    }

    /**
     * @param name parameter name
     * @param description parameter description
     * @param label parameter label
     */
    public LabelParameterValue(String name, String description, String label) {
        super(nameOrDefault(name), description);
        if (label != null) {
            this.label = label.trim();
        }
    }

    private static String nameOrDefault(String name) {
        return Util.fixEmptyAndTrim(Util.fixEmpty(name)) == null ? DEFAULT_NAME : name;
    }

    /**
     * Gets the labels to be used to trigger the next builds with
     *
     * @return the labels
     */
    public List<String> getNextLabels() {
        return Collections.unmodifiableList(nextLabels == null ? new ArrayList<>() : nextLabels);
    }

    /** Exposes the name/value as an environment variable. */
    @Override
    public void buildEnvironment(Run<?, ?> build, EnvVars env) {
        env.put(name, label);
    }

    @Override
    public Label getAssignedLabel(SubTask task) {
        return Label.get(label);
    }

    @Override
    public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
        return new VariableResolver<String>() {
            public String resolve(String name) {
                return LabelParameterValue.this.name.equals(name) ? label : null;
            }
        };
    }

    @Override
    public String toString() {
        return "[LabelParameterValue: " + name + "=" + label + "]";
    }

    /**
     * @return the label
     */
    @Exported(name = "value")
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        if (label != null) {
            this.label = label.trim();
        }
    }

    /**
     * @see hudson.model.ParameterValue#createBuildWrapper(hudson.model.AbstractBuild)
     */
    @Override
    public BuildWrapper createBuildWrapper(AbstractBuild<?, ?> build) {

        // add a badge icon to the build
        addBadgeToBuild(build);

        final ParametersDefinitionProperty property =
                build.getProject().getProperty(hudson.model.ParametersDefinitionProperty.class);
        if (property != null) {
            final List<ParameterDefinition> parameterDefinitions = property.getParameterDefinitions();
            for (ParameterDefinition paramDef : parameterDefinitions) {
                if (paramDef instanceof MultipleNodeDescribingParameterDefinition) {
                    return ((MultipleNodeDescribingParameterDefinition) paramDef).createBuildWrapper();
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

        LabelParameterValue that = (LabelParameterValue) o;

        if (!Objects.equals(label, that.label)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }

    /**
     * adds a badge to the build which will be visible in the build history as an icon
     *
     * @param build the build to add the badge to
     */
    protected void addBadgeToBuild(AbstractBuild<?, ?> build) {
        final Computer c = Computer.currentComputer();
        String controllerLabel = Jenkins.get().getSelfLabel().getName();
        if (c != null) {
            String cName = StringUtils.isBlank(c.getName()) ? controllerLabel : c.getName();
            build.addAction(
                    new LabelBadgeAction(getLabel(), Messages.LabelBadgeAction_label_tooltip_node(getLabel(), cName)));
        } else {
            build.addAction(new LabelBadgeAction(getLabel(), Messages.LabelBadgeAction_label_tooltip(getLabel())));
        }
    }
}

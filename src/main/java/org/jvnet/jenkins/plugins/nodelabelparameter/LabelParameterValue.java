/**
 *
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.Node;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Label;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.labels.LabelExpression;
import hudson.model.queue.SubTask;
import hudson.tasks.BuildWrapper;
import hudson.util.VariableResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jvnet.jenkins.plugins.nodelabelparameter.wrapper.TriggerNextBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import antlr.ANTLRException;

/**
 * 
 * @author Dominik Bartholdi (imod)
 * 
 */
public class LabelParameterValue extends ParameterValue {

    private static final Logger LOGGER       = Logger.getLogger(LabelParameterValue.class.getName());

    private static final String DEFAULT_NAME = "NODELABEL";

    @Exported(visibility = 3)
    private String              label;

    protected List<String>      nextLabels;

    public LabelParameterValue(String name) {
        super(nameOrDefault(name));
    }

    @Deprecated
    public LabelParameterValue(String name, String label) {
        this(name, label, false, false);
    }

    public LabelParameterValue(String name, List<String> labels, boolean ignoreOfflineNodes) {
        super(name);
        setNextLabels(labels, ignoreOfflineNodes);
    }

    /**
     * @param name
     */
    @DataBoundConstructor
    public LabelParameterValue(String name, String label, boolean allNodesMatchingLabel,  boolean ignoreOfflineNodes) {
        super(nameOrDefault(name));
        if (label != null) {
            this.label = label.trim();
        }

        if(allNodesMatchingLabel) {
            List<String> labels = getNodeNamesForLabelExpression(label);
            if(labels.isEmpty()) {
                // we are not able to determine a node for the given label - let Jenkins inform the user about it, by placing the job into the queue
                labels.add(label);
            }
            setNextLabels(labels, ignoreOfflineNodes);
        }
    }

    private void setNextLabels(List<String> labels, boolean ignoreOfflineNodes) {
        if (labels != null && !labels.isEmpty()) {

            nextLabels = new ArrayList<String>();
            if (ignoreOfflineNodes) {
                for (String nodeName : labels) {
                    nodeName = nodeName.trim();
                    if (NodeUtil.isNodeOnline(nodeName)) {
                        if (getLabel() == null) {
                            this.setLabel(nodeName);
                        } else {
                            nextLabels.add(nodeName);
                        }
                    } else {
                        LOGGER.fine("Skipping execution on offline node [" + nodeName + "]");
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
        if (getLabel() == null || getLabel().length() == 0) {
            // these artificial labels will cause the job to stay in the queue and the user will see this label
            if (ignoreOfflineNodes) {
                setLabel("Job triggered without a valid online node, given where: " + StringUtils.join(labels, ','));
            } else {
                setLabel("Job triggered, but no node given");
            }
        }
    }

    private List<String> getNodeNamesForLabelExpression(String labelExp) {
        List<String> nodeNames = new ArrayList<String>();
        try {
            Label label = LabelExpression.parseExpression(labelExp);
            for (Node node : label.getNodes()) {
                nodeNames.add(node.getSelfLabel().getName());
            }
        } catch (ANTLRException e) {
            LOGGER.log(Level.SEVERE, "failed to parse label ["+labelExp+"]", e);
        }
        return nodeNames;
    }

    /**
     * @param name
     * @param description
     */
    public LabelParameterValue(String name, String description, String label) {
        super(nameOrDefault(name), description);
        if (label != null) {
            this.label = label.trim();
        }
    }

    private static String nameOrDefault(String name) {
        return Util.fixEmptyAndTrim(name) == null ? DEFAULT_NAME : name;
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
     * Exposes the name/value as an environment variable.
     */
    @Override
    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
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
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        if (label != null) {
            this.label = label.trim();
        }
    }

    // /**
    // * @see hudson.model.ParameterValue#createBuildWrapper(hudson.model.AbstractBuild)
    // */
    // @Override
    // public BuildWrapper createBuildWrapper(AbstractBuild<?, ?> build) {
    // return new AddBadgeBuildWrapper();
    // }

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
                if (paramDef instanceof LabelParameterDefinition) {
                    final LabelParameterDefinition labelParameterDefinition = (LabelParameterDefinition) paramDef;
                    if (labelParameterDefinition.isAllNodesMatchingLabel()) {
                        // we expect only one node parameter definition per job
                        return new TriggerNextBuildWrapper(labelParameterDefinition);
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        LabelParameterValue that = (LabelParameterValue) o;

        if (label != null ? !label.equals(that.label) : that.label != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }

    private class AddBadgeBuildWrapper extends BuildWrapper {
        @Override
        public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
            // add a badge icon to the build
            final Computer c = Computer.currentComputer();
            if (c != null) {
                String cName = StringUtils.isBlank(c.getName()) ? "master" : c.getName();
                build.addAction(new LabelBadgeAction(getLabel(), Messages.LabelBadgeAction_label_tooltip_node(getLabel(), cName)));
            } else {
                build.addAction(new LabelBadgeAction(getLabel(), Messages.LabelBadgeAction_label_tooltip(getLabel())));
            }
            return new Environment() {
            };
        }
    }
}

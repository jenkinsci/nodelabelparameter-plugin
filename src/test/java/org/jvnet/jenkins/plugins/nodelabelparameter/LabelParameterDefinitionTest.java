package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import hudson.util.FormValidation;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.IgnoreOfflineNodeEligibility;

public class LabelParameterDefinitionTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static DumbSlave agent;

    private static final String LABEL_NAME = "my-agent-label";
    private static final LabelAtom label = new LabelAtom(LABEL_NAME);

    @BeforeClass
    public static void createAgent() throws Exception {
        agent = j.createOnlineSlave(label);
    }

    @Test
    @Deprecated
    public void testNodeParameterDefinitionDeprecated() {
        String name = "name";
        String description = "The description";
        String defaultValue = "built-in || master";
        String triggerIfResult = "The triggerIfResult value";

        LabelParameterDefinition nodeParameterDefinition =
                new LabelParameterDefinition(name, description, defaultValue, true, true, triggerIfResult);

        assertThat(nodeParameterDefinition.defaultValue, is(defaultValue));
        assertThat(nodeParameterDefinition.getName(), is(name));
        assertThat(nodeParameterDefinition.getDescription(), is(description));
        assertThat(nodeParameterDefinition.getDefaultParameterValue().getLabel(), is(defaultValue));
        assertThat(nodeParameterDefinition.getTriggerIfResult(), is(triggerIfResult));
        assertThat(nodeParameterDefinition.getNodeEligibility(), is(instanceOf(IgnoreOfflineNodeEligibility.class)));
        assertTrue(nodeParameterDefinition.isAllNodesMatchingLabel());
    }

    @Test
    @Deprecated
    public void testNodeParameterDefinitionDeprecated3Arg() {
        String name = "name";
        String description = "The description";
        String defaultValue = "built-in || master";

        LabelParameterDefinition nodeParameterDefinition =
                new LabelParameterDefinition(name, description, defaultValue);

        assertThat(nodeParameterDefinition.defaultValue, is(defaultValue));
        assertThat(nodeParameterDefinition.getName(), is(name));
        assertThat(nodeParameterDefinition.getDescription(), is(description));
        assertThat(nodeParameterDefinition.getDefaultParameterValue().getLabel(), is(defaultValue));
        assertThat(nodeParameterDefinition.getTriggerIfResult(), is("allCases"));
        assertThat(nodeParameterDefinition.getNodeEligibility(), is(instanceOf(AllNodeEligibility.class)));
        assertFalse(nodeParameterDefinition.isAllNodesMatchingLabel());
    }

    @Test
    public void testNodeParameterDefinition() {
        String name = "name";
        String description = "The description";
        String defaultValue = "built-in || master";
        String triggerIfResult = "The triggerIfResult value";

        LabelParameterDefinition nodeParameterDefinition =
                new LabelParameterDefinition(name, description, defaultValue, false, null, triggerIfResult);

        assertThat(nodeParameterDefinition.defaultValue, is(defaultValue));
        assertThat(nodeParameterDefinition.getName(), is(name));
        assertThat(nodeParameterDefinition.getDescription(), is(description));
        assertThat(nodeParameterDefinition.getDefaultParameterValue().getLabel(), is(defaultValue));
        assertThat(nodeParameterDefinition.getTriggerIfResult(), is(triggerIfResult));
        assertThat(nodeParameterDefinition.getNodeEligibility(), is(instanceOf(AllNodeEligibility.class)));
        assertFalse(nodeParameterDefinition.isAllNodesMatchingLabel());
    }

    @Test
    public void testDoListNodesForAgentLabel() throws Exception {
        LabelParameterDefinition.DescriptorImpl nodeParameterDefinition = new LabelParameterDefinition.DescriptorImpl();
        assertThat(nodeParameterDefinition.getDefaultNodeEligibility(), is(instanceOf(AllNodeEligibility.class)));
        FormValidation validation = nodeParameterDefinition.doListNodesForLabel(LABEL_NAME);
        String msg = validation.getMessage();
        assertThat(msg, allOf(containsString("Matching nodes"), containsString(agent.getNodeName())));
    }

    @Test
    public void testDoListNodesForControllerLabel() throws Exception {
        String controllerLabel = "built-in";
        LabelParameterDefinition.DescriptorImpl nodeParameterDefinition = new LabelParameterDefinition.DescriptorImpl();
        assertThat(nodeParameterDefinition.getDefaultNodeEligibility(), is(instanceOf(AllNodeEligibility.class)));
        FormValidation validation = nodeParameterDefinition.doListNodesForLabel(controllerLabel);
        String msg = validation.getMessage();
        assertThat(msg, allOf(containsString("Matching nodes"), containsString(controllerLabel)));
    }

    @Test
    public void testDoListNodesForNonExistentLabel() throws Exception {
        String badLabel = "this-label-does-not-exist";
        LabelParameterDefinition.DescriptorImpl nodeParameterDefinition = new LabelParameterDefinition.DescriptorImpl();
        assertThat(nodeParameterDefinition.getDefaultNodeEligibility(), is(instanceOf(AllNodeEligibility.class)));
        FormValidation validation = nodeParameterDefinition.doListNodesForLabel(badLabel);
        String msg = validation.getMessage();
        assertThat(msg, allOf(containsString("The label expression"), containsString(badLabel)));
    }

    @Test
    public void testDoListNodesForBlankLabel() throws Exception {
        String blankLabel = "";
        LabelParameterDefinition.DescriptorImpl nodeParameterDefinition = new LabelParameterDefinition.DescriptorImpl();
        FormValidation validation = nodeParameterDefinition.doListNodesForLabel(blankLabel);
        String msg = validation.getMessage();
        assertThat(msg, containsString("a label is required"));
    }

    @Test
    public void testDoListNodesForInvalidLabelExpression() throws Exception {
        String invalidLabel = "a||";
        LabelParameterDefinition.DescriptorImpl nodeParameterDefinition = new LabelParameterDefinition.DescriptorImpl();
        assertThat(nodeParameterDefinition.getDefaultNodeEligibility(), is(instanceOf(AllNodeEligibility.class)));
        FormValidation validation = nodeParameterDefinition.doListNodesForLabel(invalidLabel);
        String msg = validation.getMessage();
        assertThat(
                msg,
                allOf(
                        containsString("The label expression"),
                        containsString(invalidLabel),
                        containsString("is not valid")));
    }
}

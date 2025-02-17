package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import hudson.util.FormValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.IgnoreOfflineNodeEligibility;

@WithJenkins
class LabelParameterDefinitionTest {

    private static JenkinsRule j;

    private static DumbSlave agent;

    private static final String LABEL_NAME = "my-agent-label";
    private static final LabelAtom label = new LabelAtom(LABEL_NAME);

    @BeforeAll
    static void setUp(JenkinsRule rule) throws Exception {
        j = rule;
        agent = j.createOnlineSlave(label);
    }

    @Test
    @Deprecated
    void testNodeParameterDefinitionDeprecated() {
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
    void testNodeParameterDefinitionDeprecated3Arg() {
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
    void testNodeParameterDefinition() {
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
    void testDoListNodesForAgentLabel() throws Exception {
        LabelParameterDefinition.DescriptorImpl nodeParameterDefinition = new LabelParameterDefinition.DescriptorImpl();
        assertThat(nodeParameterDefinition.getDefaultNodeEligibility(), is(instanceOf(AllNodeEligibility.class)));
        FormValidation validation = nodeParameterDefinition.doListNodesForLabel(LABEL_NAME);
        String msg = validation.getMessage();
        assertThat(msg, allOf(containsString("Matching nodes"), containsString(agent.getNodeName())));
    }

    @Test
    void testDoListNodesForControllerLabel() throws Exception {
        String controllerLabel = "built-in";
        LabelParameterDefinition.DescriptorImpl nodeParameterDefinition = new LabelParameterDefinition.DescriptorImpl();
        assertThat(nodeParameterDefinition.getDefaultNodeEligibility(), is(instanceOf(AllNodeEligibility.class)));
        FormValidation validation = nodeParameterDefinition.doListNodesForLabel(controllerLabel);
        String msg = validation.getMessage();
        assertThat(msg, allOf(containsString("Matching nodes"), containsString(controllerLabel)));
    }

    @Test
    void testDoListNodesForNonExistentLabel() throws Exception {
        String badLabel = "this-label-does-not-exist";
        LabelParameterDefinition.DescriptorImpl nodeParameterDefinition = new LabelParameterDefinition.DescriptorImpl();
        assertThat(nodeParameterDefinition.getDefaultNodeEligibility(), is(instanceOf(AllNodeEligibility.class)));
        FormValidation validation = nodeParameterDefinition.doListNodesForLabel(badLabel);
        String msg = validation.getMessage();
        assertThat(
                msg,
                allOf(
                        containsString("The label expression"),
                        containsString(badLabel),
                        containsString("does not match any node")));
    }

    @Test
    void testDoListNodesForBlankLabel() throws Exception {
        String blankLabel = "";
        LabelParameterDefinition.DescriptorImpl nodeParameterDefinition = new LabelParameterDefinition.DescriptorImpl();
        FormValidation validation = nodeParameterDefinition.doListNodesForLabel(blankLabel);
        String msg = validation.getMessage();
        assertThat(msg, containsString("a label is required"));
    }

    @Test
    void testDoListNodesForInvalidLabelExpression() throws Exception {
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

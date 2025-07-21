package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.ParameterValue;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.IgnoreOfflineNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.NodeEligibility;
import org.kohsuke.stapler.StaplerRequest2;

@WithJenkins
class LabelParameterDefinitionTest {

    private static JenkinsRule j;

    private static DumbSlave agent;

    private static final String LABEL_NAME = "my-agent-label";
    private static final LabelAtom label = new LabelAtom(LABEL_NAME);

    private static final String name = "name";
    private static final String description = "The description";
    private static final String defaultValue = "built-in || master";
    private static final String triggerIfResult = "The triggerIfResult value";
    private static final boolean allNodesMatchingLabel = true;

    @BeforeAll
    static void setUp(JenkinsRule rule) throws Exception {
        j = rule;
        agent = j.createOnlineSlave(label);
    }

    @Test
    @Deprecated
    void testNodeParameterDefinitionDeprecated() {
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

    @Test
    void testCreateValue_WhenLabelIsMissingAndValueKeyIsUsed() {
        NodeEligibility nodeEligibility = mock(NodeEligibility.class);

        JSONObject jo = new JSONObject();
        jo.put("name", name);
        jo.put("value", defaultValue);
        jo.put("allNodesMatchingLabel", allNodesMatchingLabel);

        LabelParameterDefinition labelParameterDefinition = new LabelParameterDefinition(
                name, description, defaultValue, allNodesMatchingLabel, nodeEligibility, triggerIfResult);

        LabelParameterValue labelParameterValue = new LabelParameterValue(name);

        StaplerRequest2 req = mock(StaplerRequest2.class);
        when(req.bindJSON(LabelParameterValue.class, jo)).thenReturn(labelParameterValue);

        ParameterValue expectedValue = labelParameterDefinition.createValue(req, jo);

        assertThat(expectedValue, is(labelParameterValue));
        assertThat(expectedValue.getDescription(), is(labelParameterValue.getDescription()));
        assertThat(((LabelParameterValue) expectedValue).getLabel(), is(labelParameterValue.getLabel()));
        assertThat(((LabelParameterValue) expectedValue).getNextLabels(), is(notNullValue()));
    }

    @Test
    void testCreateValue_BindsLabelFromLabelKeyCorrectly() {
        NodeEligibility nodeEligibility = mock(NodeEligibility.class);

        JSONObject jo = new JSONObject();
        jo.put("name", name);
        jo.put("label", defaultValue);
        jo.put("allNodesMatchingLabel", allNodesMatchingLabel);

        LabelParameterDefinition labelParameterDefinition = new LabelParameterDefinition(
                name, description, defaultValue, allNodesMatchingLabel, nodeEligibility, triggerIfResult);

        LabelParameterValue labelParameterValue =
                new LabelParameterValue(name, defaultValue, allNodesMatchingLabel, nodeEligibility);

        StaplerRequest2 req = mock(StaplerRequest2.class);
        when(req.bindJSON(LabelParameterValue.class, jo)).thenReturn(labelParameterValue);

        ParameterValue expectedValue = labelParameterDefinition.createValue(req, jo);

        assertThat(expectedValue, is(labelParameterValue));
        assertThat(expectedValue.getDescription(), is(labelParameterValue.getDescription()));
        assertThat(((LabelParameterValue) expectedValue).getLabel(), is(labelParameterValue.getLabel()));
        assertThat(((LabelParameterValue) expectedValue).getNextLabels(), is(notNullValue()));
    }
}

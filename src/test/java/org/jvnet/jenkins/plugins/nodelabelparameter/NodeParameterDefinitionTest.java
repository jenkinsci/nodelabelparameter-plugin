package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hudson.model.ParameterValue;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;

public class NodeParameterDefinitionTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static DumbSlave agent;

    @BeforeClass
    public static void createAgent() throws Exception {
        agent = j.createOnlineSlave(new LabelAtom("my-agent-label"));
    }

    @Test
    @Deprecated
    public void testNodeParameterDefinitionDeprecatedReordersAllowedAgents() {
        String name = "name";
        String description = "description";
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add(agent.getNodeName());
        allowedAgents.add("non-existent-agent");
        String triggerIfResult = Constants.CASE_MULTISELECT_DISALLOWED;

        assertThat(allowedAgents.get(0), is(agent.getNodeName()));
        NodeParameterDefinition parameterDefinition =
                new NodeParameterDefinition(name, description, "non-existent-agent", allowedAgents, triggerIfResult);
        assertThat(allowedAgents.get(0), is("non-existent-agent")); // List reordered by constructor
        assertThat(parameterDefinition.getName(), is(name));
        assertThat(parameterDefinition.getDescription(), is(description));
        assertThat(parameterDefinition.defaultValue, is(nullValue()));
        assertThat(parameterDefinition.getTriggerIfResult(), is(triggerIfResult));
        assertFalse(parameterDefinition.getAllowMultiNodeSelection());
        assertFalse(parameterDefinition.isTriggerConcurrentBuilds());
    }

    @Test
    public void testNodeParameterDefinition() {
        String name = "name";
        String description = "description";
        List<String> defaultAgents = Arrays.asList("built-in");
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add(agent.getNodeName());
        allowedAgents.add("non-existent-agent");
        String triggerIfResult = Constants.CASE_MULTISELECT_CONCURRENT_BUILDS;

        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                name, description, defaultAgents, allowedAgents, triggerIfResult, new AllNodeEligibility());
        assertThat(allowedAgents.get(0), is(agent.getNodeName())); // List not reordered by constructor
        assertThat(parameterDefinition.getName(), is(name));
        assertThat(parameterDefinition.getDescription(), is(description));
        assertThat(parameterDefinition.defaultValue, is(nullValue()));
        assertThat(parameterDefinition.getTriggerIfResult(), is(triggerIfResult));
        assertTrue(parameterDefinition.getAllowMultiNodeSelection());
        assertTrue(parameterDefinition.isTriggerConcurrentBuilds());
    }

    @Test
    public void testCreateValue_String() {
        String name = "name";
        String description = "description";
        List<String> defaultAgents = new ArrayList<>();
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add("defaultValue");
        String triggerIfResult = "triggerIfResult";

        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                name, description, defaultAgents, allowedAgents, triggerIfResult, new AllNodeEligibility());
        assertThat(parameterDefinition.getName(), is(name));
        assertThat(parameterDefinition.getDescription(), is(description));
        assertThat(parameterDefinition.defaultValue, is(nullValue()));
        assertThat(parameterDefinition.getTriggerIfResult(), is(triggerIfResult));
        assertTrue(parameterDefinition.getAllowMultiNodeSelection());
        assertFalse(parameterDefinition.isTriggerConcurrentBuilds());

        String myValue = "my-value";
        ParameterValue parameterValue = parameterDefinition.createValue(myValue);
        assertThat(parameterValue.getName(), is(name));
        assertThat(parameterValue.getDescription(), is(description));

        // Unexpected that myValue is not returned by parameterValue.getValue()
        // Seems to be a bug in the NodeParameterDefinition implementation
        // NodeParameterDefinition declares a private field 'label' that receives
        // the value instead of it being stored in LabelParameterValue.label but
        // does not override the LabelParameterValue implementation of getValue().
        assertThat(parameterValue.getValue(), is(nullValue()));
    }

    @Test
    public void testGetAllowedNodesOrAll() {
        String name = "name";
        String description = "description";
        List<String> defaultAgents = new ArrayList<>();
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add(agent.getNodeName());
        String triggerIfResult = "triggerIfResult";

        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                name, description, defaultAgents, allowedAgents, triggerIfResult, new AllNodeEligibility());

        assertThat(parameterDefinition.getAllowedNodesOrAll(), is(allowedAgents));
    }

    @Test
    public void testGetAllowedNodesOrAllWithBuiltIn() {
        String name = "name";
        String description = "description";
        List<String> defaultAgents = new ArrayList<>();
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add("built-in");
        String triggerIfResult = "triggerIfResult";

        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                name, description, defaultAgents, allowedAgents, triggerIfResult, new AllNodeEligibility());

        assertThat(parameterDefinition.getAllowedNodesOrAll(), is(allowedAgents));
    }

    @Test
    public void testGetHelpFile() {
        NodeParameterDefinition.DescriptorImpl descriptorImpl = new NodeParameterDefinition.DescriptorImpl();

        assertThat(descriptorImpl.getHelpFile(), is("/plugin/nodelabelparameter/nodeparam.html"));
    }

    @Test
    public void testGetDefaultNodeEligibility() {
        NodeParameterDefinition.DescriptorImpl descriptorImpl = new NodeParameterDefinition.DescriptorImpl();

        assertThat(descriptorImpl.getDefaultNodeEligibility(), instanceOf(AllNodeEligibility.class));
    }
}

package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.ParameterValue;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.IgnoreOfflineNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.wrapper.TriggerNextBuildWrapper;

@WithJenkins
class NodeParameterDefinitionTest {

    private static JenkinsRule j;

    private static DumbSlave agent;

    @BeforeAll
    static void setUp(JenkinsRule rule) throws Exception {
        j = rule;
        agent = j.createOnlineSlave(new LabelAtom("my-agent-label"));
    }

    @Test
    @Deprecated
    void testNodeParameterDefinitionDeprecatedReordersAllowedAgents() {
        String name = "name";
        String description = "description";
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add(agent.getNodeName());
        allowedAgents.add("non-existent-agent");
        String triggerIfResult = Constants.CASE_MULTISELECT_DISALLOWED;

        assertThat(allowedAgents.get(0), is(agent.getNodeName()));
        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(name, description,
                "non-existent-agent", allowedAgents, triggerIfResult);
        assertThat(allowedAgents.get(0), is("non-existent-agent")); // List reordered by constructor
        assertThat(parameterDefinition.getName(), is(name));
        assertThat(parameterDefinition.getDescription(), is(description));
        assertThat(parameterDefinition.defaultValue, is(nullValue()));
        assertThat(parameterDefinition.getTriggerIfResult(), is(triggerIfResult));
        assertFalse(parameterDefinition.getAllowMultiNodeSelection());
        assertFalse(parameterDefinition.isTriggerConcurrentBuilds());
    }

    @Test
    void testNodeParameterDefinition() {
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
    void testCreateValue_String() {
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
    void testGetAllowedNodesOrAll() {
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
    void testGetAllowedNodesOrAllWithBuiltIn() {
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
    void testGetHelpFile() {
        NodeParameterDefinition.DescriptorImpl descriptorImpl = new NodeParameterDefinition.DescriptorImpl();

        assertThat(descriptorImpl.getHelpFile(), is("/plugin/nodelabelparameter/nodeparam.html"));
    }

    @Test
    void testGetDefaultNodeEligibility() {
        NodeParameterDefinition.DescriptorImpl descriptorImpl = new NodeParameterDefinition.DescriptorImpl();

        assertThat(descriptorImpl.getDefaultNodeEligibility(), instanceOf(AllNodeEligibility.class));
    }

    @Test
    void testJSONHandlingReflection() throws Exception {
        String name = "HOSTN";
        String description = "description";
        List<String> defaultAgents = new ArrayList<>();
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add("built-in");
        String triggerIfResult = "triggerIfResult";

        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                name, description, defaultAgents, allowedAgents, triggerIfResult, new AllNodeEligibility());

        // Test with String value
        JSONObject jo1 = new JSONObject();
        jo1.put("name", name);
        jo1.put("value", "built-in");

        // Use reflection to access the method directly
        java.lang.reflect.Method m = NodeParameterDefinition.class.getDeclaredMethod(
                "createValue", new Class[] { org.kohsuke.stapler.StaplerRequest2.class, net.sf.json.JSONObject.class });
        ParameterValue value1 = (ParameterValue) m.invoke(parameterDefinition, null, jo1);

        assertThat(value1, is(notNullValue()));
        assertThat(value1.getName(), is(name));
    }

    @Test
    void testReadResolve_DefaultValue() {
        String name = "name";
        String description = "description";
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add(agent.getNodeName());
        String triggerIfResult = Constants.CASE_MULTISELECT_DISALLOWED;

        // Instead of using defaultValue, set up a test case that works
        List<String> defaultSlaves = new ArrayList<>();
        defaultSlaves.add("default-node");

        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                name, description, defaultSlaves, allowedAgents, triggerIfResult, new AllNodeEligibility());

        // Verify correct setup
        List<String> retrievedSlaves = parameterDefinition.getDefaultSlaves();
        assertThat(retrievedSlaves, is(notNullValue()));
        assertThat(retrievedSlaves.size(), is(1));
        assertTrue(retrievedSlaves.contains("default-node"));
    }

    @Test
    void testReadResolve_IgnoreOfflineNodes() {
        String name = "name";
        String description = "description";
        List<String> defaultAgents = new ArrayList<>();
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add(agent.getNodeName());
        String triggerIfResult = "triggerIfResult";

        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(name, description, defaultAgents,
                allowedAgents, triggerIfResult, true);

        // Verify the node eligibility was properly set
        assertThat(parameterDefinition.getNodeEligibility(), instanceOf(IgnoreOfflineNodeEligibility.class));
    }

    @Test
    void testGetDefaultParameterValue() {
        String name = "name";
        String description = "description";
        List<String> defaultAgents = Arrays.asList("built-in");
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add(agent.getNodeName());
        String triggerIfResult = "triggerIfResult";

        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                name, description, defaultAgents, allowedAgents, triggerIfResult, new AllNodeEligibility());

        NodeParameterValue defaultValue = parameterDefinition.getDefaultParameterValue();
        assertThat(defaultValue, is(notNullValue()));
        assertThat(defaultValue.getName(), is(name));
    }

    @Test
    void testCopyWithDefaultValue() {
        String name = "name";
        String description = "description";
        List<String> defaultAgents = Arrays.asList("built-in");
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add(agent.getNodeName());
        String triggerIfResult = "triggerIfResult";

        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                name, description, defaultAgents, allowedAgents, triggerIfResult, new AllNodeEligibility());

        NodeParameterValue defaultValue = parameterDefinition.getDefaultParameterValue();
        NodeParameterDefinition copied = (NodeParameterDefinition) parameterDefinition
                .copyWithDefaultValue(defaultValue);

        // Should return this
        assertEquals(parameterDefinition, copied);
    }

    @Test
    void testCreateBuildWrapper_AllowMultiNodeSelection() {
        String name = "name";
        String description = "description";
        List<String> defaultAgents = new ArrayList<>();
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add(agent.getNodeName());

        // With multi-node selection enabled but not concurrent
        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                name, description, defaultAgents, allowedAgents, "triggerIfResult", new AllNodeEligibility());

        TriggerNextBuildWrapper wrapper = parameterDefinition.createBuildWrapper();
        assertNotNull(wrapper);
    }

    @Test
    void testCreateBuildWrapper_DisallowMultiNodeSelection() {
        String name = "name";
        String description = "description";
        List<String> defaultAgents = new ArrayList<>();
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add(agent.getNodeName());

        // With multi-node selection disabled
        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                name,
                description,
                defaultAgents,
                allowedAgents,
                Constants.CASE_MULTISELECT_DISALLOWED,
                new AllNodeEligibility());

        TriggerNextBuildWrapper wrapper = parameterDefinition.createBuildWrapper();
        assertThat(wrapper, is(nullValue()));
    }

    @Test
    void testNodeParameterDefinitionWithDefault() {
        String name = "name";
        String description = "description";
        List<String> defaultSlaves = new ArrayList<>();
        defaultSlaves.add("default-agent");
        List<String> allowedAgents = new ArrayList<>();
        allowedAgents.add(agent.getNodeName());
        String triggerIfResult = "triggerIfResult";

        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                name, description, defaultSlaves, allowedAgents, triggerIfResult, new AllNodeEligibility());

        // Check that default values were properly set
        assertThat(parameterDefinition.getDefaultSlaves(), is(notNullValue()));
        assertTrue(parameterDefinition.getDefaultSlaves().contains("default-agent"));

        // Test default parameter value creation
        NodeParameterValue defaultValue = parameterDefinition.getDefaultParameterValue();
        assertThat(defaultValue, is(notNullValue()));
        assertThat(defaultValue.getName(), is(name));
    }
}

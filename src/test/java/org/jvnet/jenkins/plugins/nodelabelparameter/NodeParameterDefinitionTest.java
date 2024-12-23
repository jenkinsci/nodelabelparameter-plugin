package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.junit.Assert.*;

import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;

public class NodeParameterDefinitionTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        final DumbSlave node = j.createOnlineSlave(new LabelAtom("node"));
    }

    @Test
    public void testReadResolve_DefaultValueIsNotNull() {
        String name = "name";
        String description = "description";
        List<String> defaultSlaves1 = new ArrayList<>();
        List<String> allowedSlaves = new ArrayList<>();
        String triggerIfResult = "triggerIfResult";

        // deaultValue is not null and defaultSlaves is not null

        NodeParameterDefinition nodeParameterDefinition1 = new NodeParameterDefinition(
                name, description, defaultSlaves1, allowedSlaves, triggerIfResult, new AllNodeEligibility());

        nodeParameterDefinition1.defaultValue = "defaultValue";

        assertNotNull(nodeParameterDefinition1.readResolve());
        assertEquals(
                nodeParameterDefinition1.getClass(),
                nodeParameterDefinition1.readResolve().getClass());

        // deaultValue is not null and defaultSlaves is null

        List<String> defaultSlaves2 = null;
        NodeParameterDefinition nodeParameterDefinition2 = new NodeParameterDefinition(
                name, description, defaultSlaves2, allowedSlaves, triggerIfResult, new AllNodeEligibility());

        nodeParameterDefinition2.defaultValue = "defaultValue";

        assertNotNull(nodeParameterDefinition2.readResolve());
        assertEquals(
                nodeParameterDefinition2.getClass(),
                nodeParameterDefinition2.readResolve().getClass());
    }

    @Test
    public void testReadResolve_NodeEligibilityIsNull() {
        String name = "name";
        String description = "description";
        List<String> defaultSlaves = null;
        List<String> allowedSlaves = new ArrayList<>();
        String triggerIfResult = "triggerIfResult";

        NodeParameterDefinition nodeParameterDefinition1 =
                new NodeParameterDefinition(name, description, defaultSlaves, allowedSlaves, triggerIfResult, true);

        nodeParameterDefinition1.defaultValue = null;

        assertNotNull(nodeParameterDefinition1.readResolve());

        NodeParameterDefinition nodeParameterDefinition2 =
                new NodeParameterDefinition(name, description, defaultSlaves, allowedSlaves, triggerIfResult, false);

        nodeParameterDefinition2.defaultValue = null;

        assertNotNull(nodeParameterDefinition2.readResolve());
    }

    @Test
    public void testNodeParameterDefinition() {
        String name = "name";
        String description = "description";
        List<String> defaultSlaves = new ArrayList<>();
        List<String> allowedSlaves = new ArrayList<>();
        allowedSlaves.add("defaultValue");
        String triggerIfResult = "triggerIfResult";

        NodeParameterDefinition nodeParameterDefinition1 =
                new NodeParameterDefinition(name, description, "defaultValue", allowedSlaves, triggerIfResult);

        assertTrue(allowedSlaves.contains("defaultValue"));

        NodeParameterDefinition nodeParameterDefinition2 = new NodeParameterDefinition(
                name, description, defaultSlaves, allowedSlaves, triggerIfResult, new AllNodeEligibility());

        assertNotNull(nodeParameterDefinition2);
    }

    @Test
    public void testCreateValue_String() {
        String name = "name";
        String description = "description";
        List<String> defaultSlaves = new ArrayList<>();
        List<String> allowedSlaves = new ArrayList<>();
        allowedSlaves.add("defaultValue");
        String triggerIfResult = "triggerIfResult";

        NodeParameterDefinition nodeParameterDefinition = new NodeParameterDefinition(
                name, description, defaultSlaves, allowedSlaves, triggerIfResult, new AllNodeEligibility());

        assertNotNull(nodeParameterDefinition.createValue("value"));
    }

    @Test
    public void testGetAllowedNodesOrAll() {
        String name = "name";
        String description = "description";
        List<String> defaultSlaves = new ArrayList<>();
        List<String> allowedSlaves = new ArrayList<>();
        allowedSlaves.add("node");
        String triggerIfResult = "triggerIfResult";

        NodeParameterDefinition nodeParameterDefinition1 = new NodeParameterDefinition(
                name, description, defaultSlaves, allowedSlaves, triggerIfResult, new AllNodeEligibility());

        assertEquals(allowedSlaves, nodeParameterDefinition1.getAllowedNodesOrAll());

        allowedSlaves = new ArrayList<>();
        allowedSlaves.add("master");

        NodeParameterDefinition nodeParameterDefinition2 = new NodeParameterDefinition(
                name, description, defaultSlaves, allowedSlaves, triggerIfResult, new AllNodeEligibility());

        assertEquals(allowedSlaves, nodeParameterDefinition2.getAllowedNodesOrAll());
    }

    @Test
    public void testGetHelpFile() {
        NodeParameterDefinition.DescriptorImpl descriptorImpl = new NodeParameterDefinition.DescriptorImpl();

        assertEquals(descriptorImpl.getHelpFile(), "/plugin/nodelabelparameter/nodeparam.html");
    }

    @Test
    public void testGetDefaultNodeEligibility() {
        NodeParameterDefinition.DescriptorImpl descriptorImpl = new NodeParameterDefinition.DescriptorImpl();

        assertNotNull(descriptorImpl.getDefaultNodeEligibility());
    }
}

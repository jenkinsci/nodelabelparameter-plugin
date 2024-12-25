package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import hudson.util.FormValidation;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class LabelParameterDefinitionTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        final DumbSlave node = j.createOnlineSlave(new LabelAtom("node"));
    }

    @Test
    public void testNodeParameterDefinition() {
        String name = "name";
        String description = "description";
        String defaultValue = "defaultValue";
        List<String> defaultSlaves = new ArrayList<>();
        List<String> allowedSlaves = new ArrayList<>();
        allowedSlaves.add("defaultValue");
        String triggerIfResult = "triggerIfResult";

        LabelParameterDefinition nodeParameterDefinition1 =
                new LabelParameterDefinition(name, description, defaultValue, true, true, triggerIfResult);

        assertEquals(nodeParameterDefinition1.defaultValue, defaultValue);

        LabelParameterDefinition nodeParameterDefinition2 =
                new LabelParameterDefinition(name, description, defaultValue, true, null, triggerIfResult);

        assertEquals(nodeParameterDefinition1.defaultValue, defaultValue);
    }

    @Test
    public void testDoListNodesForLabel() throws Exception {
        LabelParameterDefinition.DescriptorImpl nodeParameterDefinition = new LabelParameterDefinition.DescriptorImpl();

        String label = "node";

        FormValidation validation = nodeParameterDefinition.doListNodesForLabel(label);

        assertNotNull(validation);
    }
}

package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.junit.Assert.assertEquals;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class NodeParameterValueTest {
    @Test
    public void testToString() {
        NodeParameterValue nvp = new NodeParameterValue("node", "description", "label");
        assertEquals("[NodeParameterValue: node=label]", nvp.toString());
    }

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(NodeParameterValue.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .withIgnoredFields("description")
                .verify();
    }
}

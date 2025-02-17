package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class NodeParameterValueTest {
    @Test
    void testToString() {
        NodeParameterValue nvp = new NodeParameterValue("node", "description", "label");
        assertEquals("[NodeParameterValue: node=label]", nvp.toString());

        nvp.nextLabels = Arrays.asList("label1");
        assertEquals("[NodeParameterValue: node=label, nextNodes=label1]", nvp.toString());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(NodeParameterValue.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .withIgnoredFields("description")
                .verify();
    }
}

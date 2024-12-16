package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NodeParameterValueTest {
    @Test
    public void testToString() {
        NodeParameterValue nvp = new NodeParameterValue("node", "description", "label");
        assertEquals("[NodeParameterValue: node=label]", nvp.toString());
    }

    @Test
    public void testHashCode() {
        NodeParameterValue nvp = new NodeParameterValue("node", "description", "label");
        NodeParameterValue nvp2 = new NodeParameterValue("node", "description", "label");
        assertEquals(nvp.hashCode(), nvp2.hashCode());

        NodeParameterValue nvp3 = new NodeParameterValue("node", "description", "label2");
        assertNotEquals(nvp, nvp3);
    }

    @Test
    public void testEquals() {
        NodeParameterValue nvp = new NodeParameterValue("node", "description", "label");
        NodeParameterValue nvp2 = new NodeParameterValue("node", "description", "label");
        assertTrue(nvp.equals(nvp2));

        NodeParameterValue nvp3 = new NodeParameterValue("node", "description", "label2");
        assertFalse(nvp.equals(nvp3));

        assertFalse(nvp.equals(null));
        assertFalse(nvp.equals(new Object()));
        assertTrue(nvp.equals(nvp));
    }
}

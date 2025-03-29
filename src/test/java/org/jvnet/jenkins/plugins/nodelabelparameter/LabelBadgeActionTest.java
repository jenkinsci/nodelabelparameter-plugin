package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class LabelBadgeActionTest {

    @Test
    void testLabelBadgeAction() {
        // Test with a regular label and tooltip
        String testLabel = "test-label";
        String testTooltip = "This is a test tooltip";
        LabelBadgeAction badge = new LabelBadgeAction(testLabel, testTooltip);

        // Verify getIconFileName returns null (by design)
        assertNull(badge.getIconFileName());

        // Verify getUrlName returns null (by design)
        assertNull(badge.getUrlName());

        // Verify getDisplayName returns null (by design)
        assertNull(badge.getDisplayName());

        // Verify getLabel returns the label
        assertEquals(testLabel, badge.getLabel());

        // Verify getTooltip returns the tooltip
        assertEquals(testTooltip, badge.getTooltip());
    }

    @Test
    void testLabelBadgeActionWithSpecialChars() {
        // Test with a label and tooltip containing special characters
        String specialLabel = "test-label with spaces & special chars: !@#$%^&*()";
        String specialTooltip = "Tooltip with special chars: !@#$%^&*()";
        LabelBadgeAction badge = new LabelBadgeAction(specialLabel, specialTooltip);

        // Verify label and tooltip handle special characters
        assertEquals(specialLabel, badge.getLabel());
        assertEquals(specialTooltip, badge.getTooltip());
    }

    @Test
    void testLabelBadgeActionWithEmptyStrings() {
        // Test with empty label and tooltip
        LabelBadgeAction badge = new LabelBadgeAction("", "");

        // Verify behavior with empty strings
        assertEquals("", badge.getLabel());
        assertEquals("", badge.getTooltip());
    }

    @Test
    void testLabelBadgeActionWithNullValues() {
        // Test with null label and tooltip
        LabelBadgeAction badge = new LabelBadgeAction(null, null);

        // Verify null is handled gracefully
        assertNull(badge.getLabel());
        assertNull(badge.getTooltip());
    }
}

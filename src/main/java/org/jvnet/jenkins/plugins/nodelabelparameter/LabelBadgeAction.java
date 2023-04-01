/** */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.BuildBadgeAction;

/**
 * @author domi
 */
public class LabelBadgeAction implements BuildBadgeAction {

    private String label;

    private String tooltip;

    public LabelBadgeAction(String label, String tooltip) {
        this.label = label;
        this.tooltip = tooltip;
    }

    /**
     * @see hudson.model.Action#getIconFileName()
     * @return <code>null</code> as badges icons are rendered by the jelly.
     */
    @Override
    public String getIconFileName() {
        return null;
    }

    /**
     * @see hudson.model.Action#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return null;
    }

    /**
     * @see hudson.model.Action#getUrlName()
     * @return <code>null</code> as this action object doesn't need to be bound to web.
     */
    @Override
    public String getUrlName() {
        return null;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the tooltip
     */
    public String getTooltip() {
        return tooltip;
    }
}

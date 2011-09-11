/**
 * 
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.BuildBadgeAction;

/**
 * @author domi
 * 
 */
public class LabelBadgeAction implements BuildBadgeAction {

	private String label;

	public LabelBadgeAction(String label) {
		this.label = label;
	}

	/**
	 * @see hudson.model.Action#getIconFileName()
	 * 
	 * @return <code>null</code> as badges icons are rendered by the jelly.
	 */
	public String getIconFileName() {
		return null;
	}

	/**
	 * @see hudson.model.Action#getDisplayName()
	 */
	public String getDisplayName() {
		return null;
	}

	/**
	 * @see hudson.model.Action#getUrlName()
	 * 
	 * @return <code>null</code> as this action object doesn't need to be bound
	 *         to web.
	 */
	public String getUrlName() {
		return null;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

}

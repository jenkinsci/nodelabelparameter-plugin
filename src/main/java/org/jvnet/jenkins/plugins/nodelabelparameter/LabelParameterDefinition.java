/**
 * 
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.ParameterDefinition;

/**
 * Defines a build parameter used to restrict the node a job will be executed
 * on. Such a label works exactly the same way as if you would define it in the
 * UI "restrict where this job should run".
 * 
 * @author domi
 * 
 */
public class LabelParameterDefinition extends SimpleParameterDefinition {

	protected String defaultValue;

	@DataBoundConstructor
	public LabelParameterDefinition(String name, String defaultValue,
			String description) {
		super(name, description);
		this.defaultValue = defaultValue;
	}

	public LabelParameterDefinition(String name, String defaultValue) {
		this(name, defaultValue, null);
	}

	@Override
	public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) {
		if (defaultValue instanceof LabelParameterValue) {
			LabelParameterValue value = (LabelParameterValue) defaultValue;
			return new LabelParameterDefinition(getName(), value.label,
					getDescription());
		} else {
			return this;
		}
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public LabelParameterValue getDefaultParameterValue() {
		LabelParameterValue v = new LabelParameterValue(getName(),
				defaultValue, getDescription());
		return v;
	}

	@Extension
	public static class DescriptorImpl extends ParameterDescriptor {
		@Override
		public String getDisplayName() {
			return "Label";
		}

		@Override
		public String getHelpFile() {
			return "/plugin/nodelabelparameter/labelparam.html";
		}
	}

	@Override
	public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
		LabelParameterValue value = req.bindJSON(LabelParameterValue.class, jo);
		value.setDescription(getDescription());
		return value;
	}

	@Override
	public ParameterValue createValue(String value) {
		return new LabelParameterValue(getName(), value, getDescription());
	}

}

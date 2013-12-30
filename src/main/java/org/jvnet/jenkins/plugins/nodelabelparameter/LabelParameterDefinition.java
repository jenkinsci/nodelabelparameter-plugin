/**
 *
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import java.util.ArrayList;
import java.util.List;

import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.ParameterDefinition;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Defines a build parameter used to restrict the node a job will be executed
 * on. Such a label works exactly the same way as if you would define it in the
 * UI "restrict where this job should run".
 *
 * @author domi
 *
 */
public class LabelParameterDefinition extends SimpleParameterDefinition {

	public final String defaultValue;

	@DataBoundConstructor
	public LabelParameterDefinition(String name, String description, String defaultValue) {
		super(name, description);
		this.defaultValue = defaultValue;
	}

	@Override
	public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValueObj) {
		if (defaultValueObj instanceof LabelParameterValue) {
			LabelParameterValue value = (LabelParameterValue) defaultValueObj;
			return new LabelParameterDefinition(getName(), getDescription(), value.getLabel());
		} else {
			return this;
		}
	}

	@Override
	public LabelParameterValue getDefaultParameterValue() {
		return new LabelParameterValue(getName(), getDescription(), defaultValue);
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
		// JENKINS-17660 for convenience, many users use 'value' instead of label - so we make a smal hack to allow this too 
		if(StringUtils.isBlank(value.getLabel())) {
		    final String label = jo.optString("value");
		    value.setLabel(label);
		}
		return value;
	}
	
	@Override
	public ParameterValue createValue(String value) {
		return new LabelParameterValue(getName(), getDescription(), value);
	}

}

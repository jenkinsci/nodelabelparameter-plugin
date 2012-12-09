/**
 *
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.ParameterValue;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Label;
import hudson.model.queue.SubTask;
import hudson.tasks.BuildWrapper;
import hudson.util.VariableResolver;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

/**
 *
 * @author domi
 *
 */
public class LabelParameterValue extends ParameterValue {

    private static final String DEFAULT_NAME = "NODELABEL";
    
	@Exported(visibility = 3)
	private String label;

	public LabelParameterValue(String name) {
		super(nameOrDefault(name));
	}

    /**
	 * @param name
	 */
	@DataBoundConstructor
	public LabelParameterValue(String name, String label) {
		super(nameOrDefault(name));
		if (label != null) {
			this.label = label.trim();
		}
	}

	/**
	 * @param name
	 * @param description
	 */
	public LabelParameterValue(String name, String description, String label) {
		super(nameOrDefault(name), description);
		if (label != null) {
			this.label = label.trim();
		}
	}
	
	private static String nameOrDefault(String name){
	    return Util.fixEmptyAndTrim(name) == null ? DEFAULT_NAME : name;
	}

    /**
     * Exposes the name/value as an environment variable.
     */
	@Override
	public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
	    env.put(name, label);
	}

	@Override
	public Label getAssignedLabel(SubTask task) {
		return Label.get(label);
	}

	@Override
	public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
		return new VariableResolver<String>() {
			public String resolve(String name) {
				return LabelParameterValue.this.name.equals(name) ? label : null;
			}
		};
	}

	@Override
	public String toString() {
		return "[LabelParameterValue: " + name + "=" + label + "]";
	}



	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		if (label != null) {
			this.label = label.trim();
		}
	}

	/**
	 * @see hudson.model.ParameterValue#createBuildWrapper(hudson.model.AbstractBuild)
	 */
	@Override
	public BuildWrapper createBuildWrapper(AbstractBuild<?, ?> build) {
		return new AddBadgeBuildWrapper();
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LabelParameterValue that = (LabelParameterValue) o;

        if (label != null ? !label.equals(that.label) : that.label != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }

	private class AddBadgeBuildWrapper extends BuildWrapper {
		@Override
		public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
			// add a badge icon to the build
			final Computer c = Computer.currentComputer();
			if (c != null) {
				String cName = StringUtils.isBlank(c.getName()) ? "master" : c.getName();
				build.addAction(new LabelBadgeAction(getLabel(), Messages.LabelBadgeAction_label_tooltip_node(getLabel(), cName)));
			} else {
				build.addAction(new LabelBadgeAction(getLabel(), Messages.LabelBadgeAction_label_tooltip(getLabel())));
			}
			return new Environment() {
			};
		}
	}
}

package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

public interface MultipleNodeDescribingParameterDefinition {

    public static final String ALL_CASES = "allCases";
    public static final String CASE_SUCCESS = "success";
    public static final String CASE_UNSTABLE = "unstable";

    String getTriggerIfResult();

    String getName();

    boolean isIgnoreOfflineNodes();

    void validateBuild(AbstractBuild build, Launcher launcher, BuildListener listener);

}

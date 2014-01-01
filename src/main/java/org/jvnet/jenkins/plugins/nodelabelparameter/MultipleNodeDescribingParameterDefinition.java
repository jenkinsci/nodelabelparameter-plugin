package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

/**
 * 
 * @author Dominik Bartholdi (imod)
 */
public interface MultipleNodeDescribingParameterDefinition {

    /**
     * Get the requested build result definition
     * 
     * @return
     */
    public String getTriggerIfResult();

    /**
     * The name of the parameter
     * 
     * @return the name of the parameter
     */
    public String getName();

    /**
     * Should a build be triggered for nodes currently offline
     * 
     * @return <code>true</code> if offline nodes should be ignored
     */
    public boolean isIgnoreOfflineNodes();

    /**
     * Callback to allow the parameter definition to do a final validation if everything is OK to proceed. Implementations are asked to throw a runtime exception if something is not OK and the build
     * should be stopped.
     * 
     * @param build
     *            build to be validated
     * @param launcher
     *            build launcher
     * @param listener
     *            provides access to the log stream
     */
    public void validateBuild(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener);

}

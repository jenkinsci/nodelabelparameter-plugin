package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.NodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.wrapper.TriggerNextBuildWrapper;

/**
 * @author Dominik Bartholdi (imod)
 */
public interface MultipleNodeDescribingParameterDefinition {

    /**
     * Get the requested build result definition
     *
     * @return build result string that will trigger the job
     */
    String getTriggerIfResult();

    /**
     * The name of the parameter
     *
     * @return the name of the parameter
     */
    String getName();

    /**
     * gets the strategy which decides whether a node should be ignored or not
     *
     * @return the eligibility definition
     */
    NodeEligibility getNodeEligibility();

    /**
     * Callback to allow the parameter definition to do a final validation if everything is OK to
     * proceed. Implementations are asked to throw a runtime exception if something is not OK and
     * the build should be stopped.
     *
     * @param build build to be validated
     * @param launcher build launcher
     * @param listener provides access to the log stream
     */
    void validateBuild(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener);

    /**
     * Returns the wrapper to trigger the next build
     *
     * @return <code>null</code> if there is no new build to be triggered.
     */
    TriggerNextBuildWrapper createBuildWrapper();
}

package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class NextLabelCauseTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testGetShortDescription() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("projectB");
        FreeStyleBuild build = j.buildAndAssertSuccess(project);
        NextLabelCause cause = new NextLabelCause("dummylabel", build);
        String description = cause.getShortDescription();
        Assert.assertEquals(description, "A build with label/node [dummylabel] was requested");
    }

    @Test
    public void testEqualsContract() {
        // The UpstreamCause base class of NextLabelCause complicates the equals contract.
        // Intentionally use the simple() verifier.
        EqualsVerifier.simple().forClass(NextLabelCause.class).verify();
    }
}

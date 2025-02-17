package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class NextLabelCauseTest {

    @Test
    void testGetShortDescription(JenkinsRule j) throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("projectB");
        FreeStyleBuild build = j.buildAndAssertSuccess(project);
        NextLabelCause cause = new NextLabelCause("dummylabel", build);
        String description = cause.getShortDescription();
        assertEquals("A build with label/node [dummylabel] was requested", description);
    }

    @Test
    void testEqualsContract(JenkinsRule j) {
        // The UpstreamCause base class of NextLabelCause complicates the equals contract.
        // Intentionally use the simple() verifier.
        EqualsVerifier.simple().forClass(NextLabelCause.class).verify();
    }
}

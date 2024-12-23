package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class NextLabelCauseTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();
    /**
     * Tests whether a job A is able to trigger job B to be executed on a specific node/slave. If it
     * does not work, the timeout will stop/fail the test after 60 seconds.
     *
     * @throws Exception
     */
    private FreeStyleBuild build;

    @Before
    public void setup() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("projectB");
        build = j.buildAndAssertSuccess(project);
    }

    @Test
    public void testGetShortDescription() {
        NextLabelCause cause = new NextLabelCause("dummylabel", build);
        String description = cause.getShortDescription();
        Assert.assertEquals(description, "A build with label/node [dummylabel] was requested");
    }

    @Test
    public void testEqualsContract() {
        EqualsVerifier.forClass(LabelParameterValue.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .withIgnoredFields("description", "nextLabels")
                .verify();
    }
}

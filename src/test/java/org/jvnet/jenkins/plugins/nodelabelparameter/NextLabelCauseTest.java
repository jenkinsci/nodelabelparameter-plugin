package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
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
    public void testEquals() {

        NextLabelCause cause1 = new NextLabelCause("samelabel", build);
        NextLabelCause cause2 = new NextLabelCause("samelabel", build);
        NextLabelCause cause3 = new NextLabelCause("differentlabel", build);

        // cause1 and cause2 are equal while cause3 is not.
        // None of them are null.
        Assert.assertTrue("The same NextLabelCause should be equal to itself", cause1.equals(cause1));
        Assert.assertFalse("Null should not be equal to NextLabelCause", cause1.equals(null));
        Assert.assertTrue("NextLabelCauses with the same label and run should be equal", cause1.equals(cause2));
        Assert.assertFalse("NextLabelCauses with different labels should not be equal", cause1.equals(cause3));
    }
}

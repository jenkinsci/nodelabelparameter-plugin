package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
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

        NextLabelCause causeA = new NextLabelCause(null, build);
        NextLabelCause causeB = new NextLabelCause(null, build);
        /* Temporary check that hashCode does not NPE on null label */
        Assert.assertEquals(causeA.hashCode(), causeB.hashCode());
        /* Temporary check that equals does not NPE on null label */
        Assert.assertTrue(causeA.equals(causeB));
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

package org.jvnet.jenkins.plugins.nodelabelparameter;

import static hudson.Functions.isWindows;
import static org.junit.Assert.*;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.slaves.DumbSlave;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.CaptureEnvironmentBuilder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;

public class EnvironmentContributorTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private DumbSlave onlineNode1;

    @Before
    public void setUp() throws Exception {
        onlineNode1 = j.createOnlineSlave();
    }

    @After
    public void tearDown() throws Exception {
        j.jenkins.removeNode(onlineNode1);
    }

    /**
     * Makes sure 'NODE_NAME=built-in' is still available on the controller.
     */
    @Test
    public void testProjectScoped() throws Exception {

        final String nodeName = j.jenkins.getSelfLabel().getName();
        final List<String> defaultNodeNames = List.of(nodeName);
        final NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                "NODE",
                "desc",
                defaultNodeNames,
                List.of(Constants.ALL_NODES),
                Constants.CASE_MULTISELECT_DISALLOWED,
                new AllNodeEligibility());

        final FreeStyleProject p = j.createFreeStyleProject();
        final CaptureEnvironmentBuilder c = new CaptureEnvironmentBuilder();
        p.getBuildersList().add(c);

        ParametersDefinitionProperty pdp = new ParametersDefinitionProperty(parameterDefinition);
        p.addProperty(pdp);

        j.assertBuildStatusSuccess(p.scheduleBuild2(0));

        Assert.assertEquals(c.getEnvVars().get("NODE_NAME"), nodeName);
    }

    @Test
    public void jenkins19222() throws Exception {

        // set label 'clearcase' on controller
        j.jenkins.getComputer("").getNode().setLabelString("clearcase");

        FreeStyleProject projectA = (FreeStyleProject)
                j.jenkins.createProjectFromXML("projectA", getClass().getResourceAsStream("/projectA.xml"));
        final CaptureEnvironmentBuilder cA = new CaptureEnvironmentBuilder();
        projectA.getBuildersList().add(cA);

        FreeStyleProject projectB = (FreeStyleProject)
                j.jenkins.createProjectFromXML("projectB", getClass().getResourceAsStream("/projectB.xml"));
        final CaptureEnvironmentBuilder cB = new CaptureEnvironmentBuilder();
        projectA.getBuildersList().add(cB);

        Assert.assertNull(projectB.getLastBuild());

        j.assertBuildStatusSuccess(projectA.scheduleBuild2(0));
        j.waitUntilNoActivityUpTo(isWindows() ? 29000 : 10000); // 10secs on non-Windows, 29secs on Windows

        final String nodeName = j.jenkins.getSelfLabel().getName();
        Assert.assertEquals(nodeName, cA.getEnvVars().get("NODE_NAME"));
        Assert.assertEquals(nodeName, cB.getEnvVars().get("NODE_NAME"));
        Assert.assertNotNull(projectB.getLastBuild());
        Assert.assertEquals(Result.SUCCESS, projectB.getLastBuild().getResult());
    }
}

package org.jvnet.jenkins.plugins.nodelabelparameter;

import static hudson.Functions.isWindows;
import static org.junit.jupiter.api.Assertions.*;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.slaves.DumbSlave;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.CaptureEnvironmentBuilder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;

@WithJenkins
class EnvironmentContributorTest {

    private JenkinsRule j;

    private DumbSlave onlineNode1;

    @BeforeEach
    void setUp(JenkinsRule j) throws Exception {
        this.j = j;
        onlineNode1 = j.createOnlineSlave();
    }

    @AfterEach
    void tearDown() throws Exception {
        j.jenkins.removeNode(onlineNode1);
    }

    /**
     * Makes sure 'NODE_NAME=built-in' is still available on the controller.
     */
    @Test
    void testProjectScoped() throws Exception {

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

        assertEquals(c.getEnvVars().get("NODE_NAME"), nodeName);
    }

    @Test
    void jenkins19222() throws Exception {

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

        assertNull(projectB.getLastBuild());

        j.assertBuildStatusSuccess(projectA.scheduleBuild2(0));
        j.waitUntilNoActivityUpTo(isWindows() ? 29000 : 10000); // 10secs on non-Windows, 29secs on Windows

        final String nodeName = j.jenkins.getSelfLabel().getName();
        assertEquals(nodeName, cA.getEnvVars().get("NODE_NAME"));
        assertEquals(nodeName, cB.getEnvVars().get("NODE_NAME"));
        assertNotNull(projectB.getLastBuild());
        assertEquals(Result.SUCCESS, projectB.getLastBuild().getResult());
    }
}

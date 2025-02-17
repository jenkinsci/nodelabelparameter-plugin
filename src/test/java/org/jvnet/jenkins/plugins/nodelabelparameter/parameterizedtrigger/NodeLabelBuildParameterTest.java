/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import hudson.model.Action;
import hudson.model.AutoCompletionCandidates;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.labels.LabelAtom;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.ResultCondition;
import hudson.slaves.DumbSlave;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.BuildWrapper;
import hudson.util.FormValidation;
import hudson.util.StreamTaskListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.jenkins.plugins.nodelabelparameter.Constants;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelBadgeAction;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterDefinition;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterValue;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterValue;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.wrapper.TriggerNextBuildWrapper;

@WithJenkins
class NodeLabelBuildParameterTest {

    /**
     * Tests whether a job A is able to trigger job B to be executed on a specific node/slave. If it
     * does not work, the timeout will stop/fail the test after 60 seconds.
     *
     * @throws Exception
     */
    @Test
    void test(JenkinsRule j) throws Exception {

        final String paramName = "node";
        final String paramName2 = "node2";
        final String paramName3 = "node3";
        final String nodeName = "someNode" + System.currentTimeMillis();

        // create a slave with a given label to execute projectB on
        DumbSlave slave = j.createOnlineSlave(new LabelAtom(nodeName));

        // create projectA, which triggers projectB with a given label parameter
        Project<?, ?> projectA = j.createFreeStyleProject("projectA");
        projectA.getPublishersList()
                .add(new BuildTrigger(new BuildTriggerConfig(
                        "projectB", ResultCondition.SUCCESS, new NodeLabelBuildParameter(paramName, nodeName))));

        // create projectB, with a predefined parameter (same name as used in projectA!)
        FreeStyleProject projectB = j.createFreeStyleProject("projectB");

        LabelParameterDefinition lb1 =
                new LabelParameterDefinition(paramName, "some desc", "wrongNodeName", false, null, "");
        assertTrue(lb1.isTriggerConcurrentBuilds());
        assertInstanceOf(AllNodeEligibility.class, lb1.getNodeEligibility());

        LabelParameterDefinition lb2 =
                new LabelParameterDefinition(paramName2, "some desc", "wrongNodeName", false, false, "");
        assertTrue(lb2.isTriggerConcurrentBuilds());
        assertInstanceOf(AllNodeEligibility.class, lb2.getNodeEligibility());

        LabelParameterDefinition lb3 = new LabelParameterDefinition(paramName3, "some desc", "wrongNodeName");
        assertTrue(lb3.isTriggerConcurrentBuilds());
        assertInstanceOf(AllNodeEligibility.class, lb3.getNodeEligibility());

        LabelParameterDefinition lb4 =
                new LabelParameterDefinition(paramName, "some desc", "wrongNodeName", true, null, "");
        assertTrue(lb4.isTriggerConcurrentBuilds());
        assertInstanceOf(AllNodeEligibility.class, lb4.getNodeEligibility());

        ParametersDefinitionProperty pdp = new ParametersDefinitionProperty(lb1);
        ParametersDefinitionProperty pdp2 = new ParametersDefinitionProperty(lb2);
        ParametersDefinitionProperty pdp3 = new ParametersDefinitionProperty(lb3);
        ParameterValue pv1 = lb4.createValue("test");
        LabelParameterValue lpv1 = new LabelParameterValue("test");
        LabelParameterValue lpv2 = new LabelParameterValue("test2");

        projectB.addProperty(pdp);
        projectB.addProperty(pdp2);
        projectB.addProperty(pdp3);
        // CaptureEnvironmentBuilder builder = new CaptureEnvironmentBuilder();
        // projectB.getBuildersList().add(builder);
        projectB.setQuietPeriod(1);
        j.jenkins.rebuildDependencyGraph();

        // projectA should trigger projectB just after execution, therefore we
        // never trigger projectB explicitly
        projectA.scheduleBuild2(0).get();
        j.jenkins.getQueue().getItem(projectB).getFuture().get();

        FreeStyleBuild build = projectB.getLastCompletedBuild();
        String foundNodeName = build.getBuildVariables().get(paramName);
        // Assert.assertEquals(j.jenkins.getLabels(), Collections.emptySet());
        final LabelParameterDefinition.DescriptorImpl descriptor = new LabelParameterDefinition.DescriptorImpl();
        final FormValidation okDefaultValue = descriptor.doCheckDefaultValue("node");
        final FormValidation okDefaultValue2 = descriptor.doCheckDefaultValue(nodeName);
        final FormValidation badDefaultValue = descriptor.doCheckDefaultValue("  ");
        final FormValidation emptyDefaultValue = descriptor.doCheckDefaultValue("");

        final AutoCompletionCandidates candidates = descriptor.doAutoCompleteDefaultValue(paramName);
        final FormValidation doListNodesForLabel = descriptor.doListNodesForLabel(nodeName);
        String helpFile = descriptor.getHelpFile();

        assertNotNull(helpFile);
        assertEquals(lpv1, lpv1);
        assertNotEquals(lpv1, lpv2);
        assertNotEquals(new NodeParameterValue("test", "description", "TestLabel"), lpv1);
        assertInstanceOf(AllNodeEligibility.class, descriptor.getDefaultNodeEligibility());

        assertEquals(paramName, pv1.getName());
        assertEquals("allCases", lb1.getTriggerIfResult());

        List<String> labels = new ArrayList<>();
        labels.add("wrongNodeName");
        LabelParameterValue lpv3 = new LabelParameterValue("test", labels, lb1.getNodeEligibility());
        assertEquals(paramName, lb1.copyWithDefaultValue(lpv3).getName());
        assertEquals(paramName, lb1.copyWithDefaultValue(null).getName());

        assertEquals(FormValidation.Kind.OK, doListNodesForLabel.kind);
        assertEquals(candidates.getValues(), List.of(nodeName));
        assertEquals(FormValidation.Kind.WARNING, okDefaultValue.kind);
        assertEquals(FormValidation.Kind.ERROR, badDefaultValue.kind);
        assertEquals(FormValidation.Kind.OK, emptyDefaultValue.kind);
        assertEquals(FormValidation.Kind.OK, okDefaultValue2.kind);
        assertNotNull(foundNodeName, "project should run on a specific node");
        assertEquals(nodeName, foundNodeName);

        j.jenkins.removeNode(slave);
    }

    @Test
    void testLabelBadgeAction(JenkinsRule j) throws Exception {
        String paramName = "node";
        String label = "label-" + System.currentTimeMillis();

        DumbSlave slave = j.createOnlineSlave(new LabelAtom(label));

        FreeStyleProject projectA = j.createFreeStyleProject("projectA");
        LabelParameterDefinition labelParam = new LabelParameterDefinition(
                paramName, "label parameter description", "default label parameter value", true, false, "trigger");
        projectA.addProperty(new ParametersDefinitionProperty(labelParam));

        LabelParameterValue lpv = new LabelParameterValue(paramName, label, true, new AllNodeEligibility());
        FreeStyleBuild build =
                projectA.scheduleBuild2(0, new ParametersAction(lpv)).get();

        j.assertBuildStatusSuccess(build);

        LabelBadgeAction badgeAction = build.getAction(LabelBadgeAction.class);
        assertNotNull(badgeAction, "LabelBadgeAction should be added to the build");
        String toolTipText = Messages.LabelBadgeAction_label_tooltip_node(
                label, slave.getComputer().getName());
        assertEquals(label, badgeAction.getLabel());
        assertNull(badgeAction.getIconFileName());
        assertNull(badgeAction.getUrlName());
        assertNull(badgeAction.getDisplayName());
        assertEquals(toolTipText, badgeAction.getTooltip());

        j.jenkins.removeNode(slave);
    }

    @Test
    void testValidateBuildException(JenkinsRule j) throws Exception {
        String paramName = "node";
        String label = "label-" + System.currentTimeMillis();

        DumbSlave slave = j.createOnlineSlave(new LabelAtom(label));

        FreeStyleProject projectA = j.createFreeStyleProject("projectA");
        // When concurrent builds are allowed and the triggerIfResult parameter is not ALL_CASES,
        // then job will fail and validateBuild will throw IllegalStateException
        projectA.setConcurrentBuild(true);
        LabelParameterDefinition labelParam = new LabelParameterDefinition(
                paramName, "label parameter description", "default label parameter value", true, false, "trigger");
        projectA.addProperty(new ParametersDefinitionProperty(labelParam));

        LabelParameterValue lpv = new LabelParameterValue(paramName, label, true, new AllNodeEligibility());
        FreeStyleBuild build =
                projectA.scheduleBuild2(0, new ParametersAction(lpv)).get();

        // Increase coverage by testing NodeParameterValue equals method
        NodeParameterValue npv = new NodeParameterValue(paramName, "node parameter value description", label);
        assertNotEquals(npv, lpv);

        // Build is expected to fail - see failure message in validateBuild message assertion
        j.assertBuildStatus(Result.FAILURE, build);

        BuildWrapper buildWrapper = lpv.createBuildWrapper(build);
        assertInstanceOf(TriggerNextBuildWrapper.class, buildWrapper);
        assertEquals(BuildStepMonitor.BUILD, ((TriggerNextBuildWrapper) buildWrapper).getRequiredMonitorService());

        IllegalStateException e =
                assertThrows(IllegalStateException.class, () -> labelParam.validateBuild(build, null, null));
        assertEquals(
                "The project is configured to run builds concurrent, but the node parameter [node] is configured to trigger new builds depending on the state of the last build only!",
                e.getMessage());

        j.jenkins.removeNode(slave);
    }

    @Test
    void testValidateBuildNoExceptionIfConcurrentBuildsAllowed(JenkinsRule j) throws Exception {
        String paramName = "node";
        String label = "label-" + System.currentTimeMillis();

        DumbSlave slave = j.createOnlineSlave(new LabelAtom(label));

        FreeStyleProject projectA = j.createFreeStyleProject("projectA");
        // If concurrent builds are allowed, then ALL_CASES is
        // required to avoid IllegalStateException from validateBuild
        projectA.setConcurrentBuild(true);
        LabelParameterDefinition labelParam = new LabelParameterDefinition(
                paramName,
                "label parameter description",
                "default label parameter value",
                true,
                false,
                Constants.ALL_CASES);
        projectA.addProperty(new ParametersDefinitionProperty(labelParam));

        LabelParameterValue lpv = new LabelParameterValue(paramName, label, true, new AllNodeEligibility());
        FreeStyleBuild build =
                projectA.scheduleBuild2(0, new ParametersAction(lpv)).get();

        j.assertBuildStatusSuccess(build);

        // Confirm exception is not thrown wwhen triggerIfResult is ALL_CASES and concurrent builds are allowed
        labelParam.validateBuild(build, null, null);

        j.jenkins.removeNode(slave);
    }

    @Test
    void testMacroEvaluationExceptionHandling(JenkinsRule j) throws Exception {
        String name = "Dummy";
        String nodeLabel = "${TEST, arg = 'a'}";
        NodeLabelBuildParameter nodeLabelBuildParameter = new NodeLabelBuildParameter(name, nodeLabel);

        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        TaskListener listener = new StreamTaskListener(new PrintStream(logStream), StandardCharsets.UTF_8);

        FreeStyleProject project = j.createFreeStyleProject("projectB");
        FreeStyleBuild build = j.buildAndAssertSuccess(project);

        Action result = nodeLabelBuildParameter.getAction(build, listener);
        assertNotNull(result, "Expected a parameter action result");
        assertThat(result, is(instanceOf(ParametersAction.class)));

        // MacroEvaluationException is logged due to error processing tokens
        String loggedOutput = logStream.toString();
        assertThat(loggedOutput, containsString("org.jenkinsci.plugins.tokenmacro.MacroEvaluationException"));
        assertThat(loggedOutput, containsString("Error processing tokens"));
    }

    @Test
    void testGetDisplayName(JenkinsRule j) {
        NodeLabelBuildParameter.DescriptorImpl descriptor = new NodeLabelBuildParameter.DescriptorImpl();

        assertEquals("NodeLabel parameter", descriptor.getDisplayName());
    }
}

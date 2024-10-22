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

import hudson.model.AutoCompletionCandidates;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterValue;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.model.labels.LabelAtom;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.ResultCondition;
import hudson.slaves.DumbSlave;
import hudson.util.FormValidation;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterDefinition;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterValue;
import org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterValue;

public class NodeLabelBuildParameterTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    /**
     * Tests whether a job A is able to trigger job B to be executed on a specific node/slave. If it
     * does not work, the timeout will stop/fail the test after 60 seconds.
     *
     * @throws Exception
     */
    @Test
    public void test() throws Exception {

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
        LabelParameterDefinition lb2 =
                new LabelParameterDefinition(paramName2, "some desc", "wrongNodeName", false, false, "");
        LabelParameterDefinition lb3 = new LabelParameterDefinition(paramName3, "some desc", "wrongNodeName");
        LabelParameterDefinition lb4 =
                new LabelParameterDefinition(paramName, "some desc", "wrongNodeName", true, null, "");

        ParametersDefinitionProperty pdp = new ParametersDefinitionProperty(lb1);
        ParametersDefinitionProperty pdp2 = new ParametersDefinitionProperty(lb2);
        ParametersDefinitionProperty pdp3 = new ParametersDefinitionProperty(lb3);
        ParameterValue pv1 = lb4.createValue("test");
        LabelParameterValue lpv1 = new LabelParameterValue("test");
        LabelParameterValue lpv2 = new LabelParameterValue("test2");
        List<String> labels = new ArrayList<>();
        labels.add("wrongNodeName");
        LabelParameterValue lpv3 = new LabelParameterValue("test", labels, lb1.getNodeEligibility());
        NodeParameterValue npv1 = new NodeParameterValue("test", "description", "TestLabel");
        NodeParameterValue npv2 = new NodeParameterValue("test", "description", "wrongNodeName");
        String resultTrigger = lb1.getTriggerIfResult();
        lb1.isTriggerConcurrentBuilds();
        lb1.getNodeEligibility();

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

        Assert.assertTrue(lpv1.equals(lpv1));
        Assert.assertFalse(lpv1.equals(null));
        Assert.assertFalse(lpv1.equals(lpv2));
        Assert.assertFalse(lpv1.equals(npv1));
        //Assert.assertTrue(lpv3.equals(npv2));

        Assert.assertEquals(pv1.getName(), paramName);
        Assert.assertEquals(resultTrigger, "allCases");
        Assert.assertEquals(
                lb1.copyWithDefaultValue(new LabelParameterValue("")).getName(), paramName);

        Assert.assertEquals(lb1.copyWithDefaultValue(null).getName(), paramName);

        Assert.assertEquals(doListNodesForLabel.kind, FormValidation.Kind.OK);
        Assert.assertEquals(candidates.getValues(), List.of(nodeName));
        Assert.assertEquals(okDefaultValue.kind, FormValidation.Kind.WARNING);
        Assert.assertEquals(badDefaultValue.kind, FormValidation.Kind.ERROR);
        Assert.assertEquals(emptyDefaultValue.kind, FormValidation.Kind.OK);
        Assert.assertEquals(okDefaultValue2.kind, FormValidation.Kind.OK);
        Assert.assertNotNull("project should run on a specific node", foundNodeName);
        Assert.assertEquals(nodeName, foundNodeName);

        j.jenkins.removeNode(slave);
    }
}

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

import hudson.model.Cause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.QueueTaskFuture;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.ResultCondition;
import hudson.slaves.DumbSlave;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.jenkins.plugins.nodelabelparameter.Constants;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterDefinition;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.IgnoreOfflineNodeEligibility;

public class NodeLabelBuildParameterTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    /**
     * Tests whether a job A is able to trigger job B to be executed on a specific node/slave. If it does not work, the timeout will stop/fail the test after 60 seconds.
     * 
     * @throws Exception
     */
    @Test
    public void test() throws Exception {

        final String paramName = "node";
        final String nodeName = "someNode" + System.currentTimeMillis();

        // create a slave with a given label to execute projectB on
        DumbSlave slave = j.createOnlineSlave(new LabelAtom(nodeName));

        // create projectA, which triggers projectB with a given label parameter
        Project<?, ?> projectA = j.createFreeStyleProject("projectA");
        projectA.getPublishersList().add(new BuildTrigger(new BuildTriggerConfig("projectB", ResultCondition.SUCCESS, new NodeLabelBuildParameter(paramName, nodeName))));

        // create projectB, with a predefined parameter (same name as used in projectA!)
        FreeStyleProject projectB = j.createFreeStyleProject("projectB");
        ParametersDefinitionProperty pdp = new ParametersDefinitionProperty(new LabelParameterDefinition(paramName, "some desc", "wrongNodeName"));
        projectB.addProperty(pdp);
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
        Assert.assertNotNull("project should run on a specific node", foundNodeName);
        Assert.assertEquals(nodeName, foundNodeName);

        j.jenkins.removeNode(slave);

    }
    
    @Test
    @Bug(22226)
    public void testLabelsOnTheDisabledExecutor() throws Exception {
        final String paramName = "node";
        final String nodeName = "someNode" + System.currentTimeMillis();

        // Create a slave with a given label to execute projectB on
        DumbSlave slave = j.createSlave(new LabelAtom(nodeName));
        slave.toComputer().doToggleOffline("Just4test");
        
        // Create and submit the project    
        LabelParameterDefinition param = new LabelParameterDefinition(
                paramName, "some desc", nodeName, true, 
                new IgnoreOfflineNodeEligibility(), Constants.ALL_CASES);
        Project<?, ?> project = j.createFreeStyleProject();
        ParametersDefinitionProperty pdp = new ParametersDefinitionProperty(param);
        project.addProperty(pdp);
        
        // Schedule the job with the explicitly-defined parameter
        QueueTaskFuture task = project.scheduleBuild2(0, new Cause.UserIdCause(), 
                new ParametersAction(param.createValue(nodeName)));
        Thread.sleep(5000);
        Assert.assertFalse("Task has been finished, but it should hang in queue", task.isDone());
        
        // Enable slave and check the task status
        slave.toComputer().setTemporarilyOffline(false);
        try {
            task.get(5000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException timeout) {
            timeout.printStackTrace();
            Assert.fail("Jobs have not been executed within the timeout");
        }        
    }
}

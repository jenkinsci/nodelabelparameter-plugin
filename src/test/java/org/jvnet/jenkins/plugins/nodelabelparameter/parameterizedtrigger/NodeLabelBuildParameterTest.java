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

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.model.labels.LabelAtom;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.ResultCondition;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterDefinition;

public class NodeLabelBuildParameterTest extends HudsonTestCase {

	/**
	 * Tests whether a job A is able to trigger job B to be executed on a
	 * specific node/slave. If it does not work, the timeout will stop/fail the
	 * test after 60 seconds.
	 * 
	 * @throws Exception
	 */
	public void test() throws Exception {

		final String paramName = "node";
		final String nodeName = "someNode";

		// create a slave with a given label to execute projectB on
		createOnlineSlave(new LabelAtom(nodeName));

		// create projectA, which triggers projectB with a given label parameter
		Project<?, ?> projectA = createFreeStyleProject("projectA");
		projectA.getPublishersList().add(
				new BuildTrigger(new BuildTriggerConfig("projectB",
						ResultCondition.SUCCESS, new NodeLabelBuildParameter(
								paramName, nodeName))));

		// create projectB, with a predefined parameter (same name as used in
		// projectA!)
		FreeStyleProject projectB = createFreeStyleProject("projectB");
		ParametersDefinitionProperty pdp = new ParametersDefinitionProperty(
				new LabelParameterDefinition(paramName, "some desc",
						"wrongNodeName"));
		projectB.addProperty(pdp);
		// CaptureEnvironmentBuilder builder = new CaptureEnvironmentBuilder();
		// projectB.getBuildersList().add(builder);
		projectB.setQuietPeriod(1);
		hudson.rebuildDependencyGraph();

		// projectA should trigger projectB just after execution, therefore we
		// never trigger projectB explicitly
		projectA.scheduleBuild2(0).get();
		hudson.getQueue().getItem(projectB).getFuture().get();

		FreeStyleBuild build = projectB.getLastCompletedBuild();
		String foundNodeName = build.getBuildVariables().get(paramName);
		assertNotNull("project should run on a specific node", foundNodeName);
		assertEquals(nodeName, foundNodeName);

	}
}

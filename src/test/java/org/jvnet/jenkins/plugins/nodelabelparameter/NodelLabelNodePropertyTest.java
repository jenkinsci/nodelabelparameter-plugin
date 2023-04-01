package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.IgnoreOfflineNodeEligibility;

/**
 * @author Dominik Bartholdi (imod)
 */
public class NodelLabelNodePropertyTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private String controllerLabel = null;

    private DumbSlave onlineNode1;
    private DumbSlave onlineNode2;
    private DumbSlave offlineNode;

    @Before
    public void setUp() throws Exception {
        onlineNode1 = j.createOnlineSlave(new LabelAtom("mylabel1"));
        onlineNode2 = j.createOnlineSlave(new LabelAtom("mylabel2"));
        offlineNode = j.createOnlineSlave(new LabelAtom("mylabel3"));
        offlineNode.getComputer().setTemporarilyOffline(true, new hudson.slaves.OfflineCause.ByCLI("mark offline"));
        controllerLabel = j.jenkins.getSelfLabel().getName();
    }

    @After
    public void tearDown() throws Exception {
        j.jenkins.removeNode(onlineNode1);
        j.jenkins.removeNode(onlineNode2);
        j.jenkins.removeNode(offlineNode);
    }

    /**
     * usescase: job is configured to be executed concurrent on three nodes per default, three nodes
     * are online - but one of these is marked as temp offline
     *
     * @throws Exception
     */
    @Test
    public void jobMustRunOnAllRequestedSlaves_IgnoreTempOfflineNodes() throws Exception {

        assertTrue(NodeUtil.isNodeOnline(onlineNode1.getNodeName()));
        assertTrue(NodeUtil.isNodeOnline(onlineNode2.getNodeName()));
        assertFalse(NodeUtil.isNodeOnline(offlineNode.getNodeName()));

        final List<String> defaultNodeNames =
                Arrays.asList(onlineNode1.getNodeName(), offlineNode.getNodeName(), onlineNode2.getNodeName());
        runTest(
                2,
                0,
                true,
                new NodeParameterDefinition(
                        "NODE",
                        "desc",
                        defaultNodeNames,
                        Collections.singletonList(Constants.ALL_NODES),
                        Constants.CASE_MULTISELECT_CONCURRENT_BUILDS,
                        new IgnoreOfflineNodeEligibility()));
    }

    /**
     * usescase: job is configured to be executed concurrent on three nodes per default, three nodes
     * are online - but one of these is marked as temp offline
     *
     * @throws Exception
     */
    @Test
    public void jobMustRunOnAllRequestedSlaves_DontIgnoreTempOfflineNodes() throws Exception {

        assertTrue(NodeUtil.isNodeOnline(onlineNode1.getNodeName()));
        assertTrue(NodeUtil.isNodeOnline(onlineNode2.getNodeName()));
        assertFalse(NodeUtil.isNodeOnline(offlineNode.getNodeName()));

        final List<String> defaultNodeNames = Arrays.asList(
                offlineNode.getNodeName(), onlineNode2.getNodeName(), onlineNode1.getNodeName(), controllerLabel);
        runTest(
                3,
                1,
                true,
                new NodeParameterDefinition(
                        "NODE",
                        "desc",
                        defaultNodeNames,
                        Collections.singletonList(Constants.ALL_NODES),
                        Constants.CASE_MULTISELECT_CONCURRENT_BUILDS,
                        new AllNodeEligibility()));
    }

    protected void runTest(
            int expectedNumberOfExecutedRuns,
            int expectedNumberOfItemsInTheQueue,
            boolean configureProjectForConcurrentBuilds,
            NodeParameterDefinition parameterDefinition)
            throws Exception {

        FreeStyleProject projectA = j.createFreeStyleProject("projectA");
        projectA.setConcurrentBuild(configureProjectForConcurrentBuilds);

        ParametersDefinitionProperty pdp = new ParametersDefinitionProperty(parameterDefinition);
        projectA.addProperty(pdp);

        j.assertBuildStatus(
                Result.SUCCESS,
                projectA.scheduleBuild2(0, new Cause.UserIdCause()).get());
        // we can't wait for no activity, as this would also wait for the jobs we expect to stay in
        // the queue
        // j.waitUntilNoActivity();
        // Sleep up to 10 seconds
        int counter = 0;
        do {
            Thread.sleep(1003); // give async triggered jobs some time to finish (1 second)
        } while (++counter < 10 && projectA.getLastBuild().number < expectedNumberOfExecutedRuns);
        assertEquals("expcted number of runs", expectedNumberOfExecutedRuns, projectA.getLastBuild().number);
        assertEquals(
                "expected number of items in the queue",
                expectedNumberOfItemsInTheQueue,
                j.jenkins.getQueue().getBuildableItems().size());
        assertThat("Full sleep time consumed", counter, is(lessThan(10)));
    }
}

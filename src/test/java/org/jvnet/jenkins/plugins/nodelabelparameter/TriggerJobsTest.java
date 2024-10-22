package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jenkins.model.ParameterizedJobMixIn;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;

/**
 * @author Dominik Bartholdi (imod)
 */
public class TriggerJobsTest {

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
     * use case: job is configured to be executed on three nodes per default, only two nodes are
     * online - offline nodes must be ignored
     *
     * @throws Exception
     */
    @Test
    public void jobMustRunOnAllRequestedAgents_IgnoreOfflineNodes() throws Exception {

        final List<String> defaultNodeNames =
                Arrays.asList(onlineNode1.getNodeName(), offlineNode.getNodeName(), onlineNode2.getNodeName());
        runTest(
                2,
                0,
                false,
                new NodeParameterDefinition(
                        "NODE",
                        "desc",
                        defaultNodeNames,
                        Collections.singletonList(Constants.ALL_NODES),
                        Constants.ALL_CASES,
                        true));
    }

    /**
     * use case: job is configured to be executed on three nodes per default, only two nodes are
     * online - offline nodes are NOT ignored
     *
     * @throws Exception
     */
    @Test
    public void jobMustRunOnAllRequestedAgents_NotIgnoringOfflineNodes() throws Exception {

        /* Test is unreliable on Windows since inclusive naming support was added */
        if (isWindows()) {
            return;
        }
        final List<String> defaultNodeNames =
                Arrays.asList(onlineNode1.getNodeName(), offlineNode.getNodeName(), onlineNode2.getNodeName());
        runTest(
                2,
                1,
                false,
                new NodeParameterDefinition(
                        "NODE",
                        "desc",
                        defaultNodeNames,
                        Collections.singletonList(Constants.ALL_NODES),
                        Constants.ALL_CASES,
                        false));
    }

    /**
     * use case: job is configured to be executed on three nodes per default (concurrent), only two
     * nodes are online - offline nodes are NOT ignored
     *
     * @throws Exception
     */
    @Test
    public void jobMustRunOnAllRequestedAgents_Concurrent_NotIgnoringOfflineNodes() throws Exception {

        final List<String> defaultNodeNames =
                Arrays.asList(onlineNode1.getNodeName(), offlineNode.getNodeName(), onlineNode2.getNodeName());
        runTest(
                2,
                1,
                true,
                new NodeParameterDefinition(
                        "NODE",
                        "desc",
                        defaultNodeNames,
                        Collections.singletonList(Constants.ALL_NODES),
                        Constants.CASE_MULTISELECT_CONCURRENT_BUILDS,
                        false));
    }

    /**
     * use case: job is configured to be executed on three nodes per default (concurrent), only two
     * nodes are online - offline nodes are NOT ignored
     *
     * @throws Exception
     */
    @Test
    public void jobMustRunOnAllRequestedAgents_Concurrent_IgnoreOfflineNodes() throws Exception {

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
                        true));
    }

    /**
     * use case: job is configured to be executed on four nodes per default (concurrent), only two
     * nodes and controller are online
     *
     * @throws Exception
     */
    @Test
    public void jobMustRunOnAllRequestedAgents_including_Node_on_Controller_IgnoreOfflineNodes() throws Exception {

        final List<String> defaultNodeNames = Arrays.asList(
                controllerLabel, onlineNode1.getNodeName(), offlineNode.getNodeName(), onlineNode2.getNodeName());

        NodeParameterDefinition nodeParameterDefinition = new NodeParameterDefinition(
                "NODE",
                "desc",
                defaultNodeNames,
                Collections.singletonList(Constants.ALL_NODES),
                Constants.ALL_CASES,
                true);

        Assert.assertEquals(
                nodeParameterDefinition.getAllowedNodesOrAll(), List.of("built-in", "slave0", "slave1", "slave2"));

        runTest(3, 0, false, nodeParameterDefinition);
    }

    protected void runTest(
            int expectedNumberOfExecutedRuns,
            int expectedNumberOfItemsInTheQueue,
            boolean configureProjectForConcurrentBuilds,
            NodeParameterDefinition parameterDefinition)
            throws Exception {

        assertTrue(NodeUtil.isNodeOnline(onlineNode1.getNodeName()));
        assertTrue(NodeUtil.isNodeOnline(onlineNode2.getNodeName()));
        assertFalse(NodeUtil.isNodeOnline(offlineNode.getNodeName()));

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
        assertEquals("Number of builds", expectedNumberOfExecutedRuns, projectA.getLastBuild().number);
        assertEquals(
                "Pending items: " + j.jenkins.getQueue().getPendingItems(),
                0,
                j.jenkins.getQueue().getPendingItems().size());
        assertThat("Full sleep time consumed", counter, is(lessThan(10)));
        assertEquals(
                "Queued build count",
                expectedNumberOfItemsInTheQueue,
                j.jenkins.getQueue().countBuildableItems());
    }

    /**
     * Test that we are able to trigger a job via curl, passing 'value' as value key.
     *
     * @see https://github.com/jenkinsci/nodelabelparameter-plugin/pull/12
     * @see https://issues.jenkins-ci.org/browse/JENKINS-28374
     */
    @Test
    public void testTriggerViaCurlWithValue() throws Exception {
        FreeStyleProject projectA = j.createFreeStyleProject("projectA");
        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                "NODE",
                "desc",
                Collections.singletonList(controllerLabel),
                Collections.singletonList(onlineNode1.getNodeName()),
                null,
                new AllNodeEligibility());
        String json = "{\"parameter\":[{\"name\":\"NODE\",\"value\":[\"" + controllerLabel + "\"]}]}";
        runTestViaCurl(projectA, parameterDefinition, json, 1, Result.SUCCESS);
    }

    /**
     * Test that we are able to trigger a job via curl, passing 'label' as value key.
     *
     * @see https://github.com/jenkinsci/nodelabelparameter-plugin/pull/12
     * @see https://issues.jenkins-ci.org/browse/JENKINS-28374
     */
    @Test
    public void testTriggerViaCurlWithLabel() throws Exception {
        FreeStyleProject projectA = j.createFreeStyleProject("projectA");
        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                "NODE",
                "desc",
                Collections.singletonList(controllerLabel),
                Collections.singletonList(onlineNode1.getNodeName()),
                null,
                new AllNodeEligibility());
        String json = "{\"parameter\":[{\"name\":\"NODE\",\"label\":[\"" + controllerLabel + "\"]}]}";
        parameterDefinition.createValue("test");
        LabelParameterValue lpv = new LabelParameterValue("test");
        ParameterDefinition copy = parameterDefinition.copyWithDefaultValue(lpv);
        assertThat(copy.getDescription(), is(parameterDefinition.getDescription()));
        assertThat(copy.getType(), is(parameterDefinition.getType()));
        assertThat(copy.getName(), is(parameterDefinition.getName()));
        assertThat(
                NodeParameterDefinition.getSlaveNamesForSelection(),
                contains(
                        Constants.ALL_NODES,
                        controllerLabel,
                        onlineNode1.getNodeName(),
                        onlineNode2.getNodeName(),
                        offlineNode.getNodeName()));
        assertThat(
                NodeParameterDefinition.getSlaveNames(),
                contains(
                        controllerLabel,
                        onlineNode1.getNodeName(),
                        onlineNode2.getNodeName(),
                        offlineNode.getNodeName()));
        runTestViaCurl(projectA, parameterDefinition, json, 1, Result.SUCCESS);
    }

    /**
     * Test that we are able to trigger a job via curl, passing 'labels' as value key.
     *
     * @see https://github.com/jenkinsci/nodelabelparameter-plugin/pull/12
     * @see https://issues.jenkins-ci.org/browse/JENKINS-28374
     */
    @Test
    public void testTriggerViaCurlWithLabels() throws Exception {
        FreeStyleProject projectA = j.createFreeStyleProject("projectA");
        NodeParameterDefinition parameterDefinition = new NodeParameterDefinition(
                "NODE",
                "desc",
                Collections.singletonList(controllerLabel),
                Collections.singletonList(onlineNode1.getNodeName()),
                null,
                new AllNodeEligibility());
        String json = "{\"parameter\":[{\"name\":\"NODE\",\"labels\":[\"" + controllerLabel + "\"]}]}";
        runTestViaCurl(projectA, parameterDefinition, json, 1, Result.SUCCESS);
    }

    /**
     * Run a test via pseudo-curl. Instead of forking a process and calling the curl utility, we
     * emulate what curl would do, but using a HTTP library in Java, adding security crumb
     * parameter, and the form parameters normally submitted in Jenkins.
     *
     * <p>Assertions are made according to the expected values passed as parameter by the user.
     *
     * @param project a Jenkins project
     * @param parameterDefinition our parameter definition
     * @param json JSON String
     * @param expectedBuildNumber expected build number in Jenkins (starts from 1)
     * @param expectedResult expected build result after the job is triggered
     * @throws Exception when executing requests and also while using the JenkinsRule and WebClient
     *     methods
     */
    private <JobT extends Job<JobT, RunT> & ParameterizedJobMixIn.ParameterizedJob, RunT extends Run<JobT, RunT>>
            void runTestViaCurl(
                    JobT project,
                    NodeParameterDefinition parameterDefinition,
                    String json,
                    int expectedBuildNumber,
                    Result expectedResult)
                    throws Exception {
        ParametersDefinitionProperty pdp = new ParametersDefinitionProperty(parameterDefinition);
        project.addProperty(pdp);
        // URL triggered, see the plugin documentation for more information
        String triggerUrl = String.format("%s%sbuild", j.getURL(), project.getUrl());

        JenkinsRule.WebClient wc = j.createWebClient();
        URL url = new URL(triggerUrl);
        WebRequest requestSettings = new WebRequest(url, HttpMethod.POST);
        // add security crumb (cannot modify the list after that, so we recreate the parameters
        // right away)
        wc.addCrumb(requestSettings);
        List<org.htmlunit.util.NameValuePair> requestParameters = new ArrayList<>();
        requestParameters.add(new org.htmlunit.util.NameValuePair("json", json));
        requestParameters.add(new org.htmlunit.util.NameValuePair("Submit", "Build"));
        requestParameters.addAll(requestSettings.getRequestParameters());
        requestSettings.setRequestParameters(requestParameters);

        Page page = wc.getPage(requestSettings);
        // wait page to have been fully loaded
        j.waitUntilNoActivity();
        // get the last build
        RunT b = project.getLastBuild();
        // assert we got it right
        assertEquals(expectedBuildNumber, b.number);
        assertEquals(expectedResult, b.getResult());
    }

    private boolean isWindows() {
        return java.io.File.pathSeparatorChar == ';';
    }
}

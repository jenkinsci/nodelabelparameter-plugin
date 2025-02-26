package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.StreamBuildListener;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactory;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters.DontTriggerException;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BlockingBehaviour;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.slaves.DumbSlave;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;

@WithJenkins
class AllNodesForLabelBuildParameterFactoryUnitTest {

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule j) throws Exception {
        this.j = j;
        j.createSlave("node1", "otherlabel", new EnvVars());
        j.createSlave("node2", "label", new EnvVars());
        final DumbSlave n3 = j.createSlave("node3", "label", new EnvVars());
    }

    /**
     * Check {@link AllNodesForLabelBuildParameterFactory} behaviors.
     * These are combined into one, otherwise
     * #noMatchingNodeShouldYieldSameLabel would be stuck in the queue
     * and the test would never finish.
     *
     * @throws Exception
     */
    @Test
    void labelParameterFactoriesMustOnlyCreateValidParameters() throws Exception {

        final AllNodesForLabelBuildParameterFactory twoNodesFactory =
                new AllNodesForLabelBuildParameterFactory("LABEL", "label", false);
        final AllNodesForLabelBuildParameterFactory dummyNodesFactory =
                new AllNodesForLabelBuildParameterFactory("LABEL", "dummy", false);

        FreeStyleProject projectA = j.createFreeStyleProject("projectA");
        FreeStyleProject projectB = j.createFreeStyleProject("projectB");

        final List<Boolean> executed = new ArrayList<>();

        projectA.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                    throws InterruptedException, IOException {
                try {

                    shouldGetParameterForEachMatchingNode(twoNodesFactory, build, listener);
                    noMatchingNodeShouldYieldSameLabel(dummyNodesFactory, build, listener);

                } catch (DontTriggerException e) {
                    e.printStackTrace();
                    return fail(e.getMessage());
                }

                executed.add(Boolean.TRUE);
                return true;
            }
        });

        projectA.getBuildersList().add(createTriggerBuilder(projectB, twoNodesFactory));
        j.assertBuildStatus(
                Result.SUCCESS,
                projectA.scheduleBuild2(0, new Cause.UserIdCause()).get());
        // make sure the test was really executed
        assertThat(executed, contains(Boolean.TRUE));
    }

    @Test
    void testGetDisplayName() {
        AllNodesForLabelBuildParameterFactory.DescriptorImpl descriptor =
                new AllNodesForLabelBuildParameterFactory.DescriptorImpl();

        assertEquals("All Nodes for Label Factory", descriptor.getDisplayName());
    }

    @Test
    void testIsIgnoreOfflineNodes() {
        AllNodesForLabelBuildParameterFactory factory =
                new AllNodesForLabelBuildParameterFactory("name", "label", true);

        // Assert that the ignoreOfflineNodes flag is set to true
        assertTrue(factory.isIgnoreOfflineNodes());

        factory = new AllNodesForLabelBuildParameterFactory("name", "label", false);

        // Assert that the ignoreOfflineNodes flag is set to false
        assertFalse(factory.isIgnoreOfflineNodes());
    }

    @Test
    void testMacroEvaluationExceptionHandling() throws Exception {
        FreeStyleProject projectA = j.createFreeStyleProject("projectA");
        AbstractBuild<?, ?> build = projectA.scheduleBuild2(0).get();

        // Setup log capture
        String malformedMacro = "${UNCLOSED_MACRO";
        AllNodesForLabelBuildParameterFactory factory =
                new AllNodesForLabelBuildParameterFactory("LABEL", malformedMacro, false);

        // Setup log capture to verify exception handling
        ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
        BuildListener listener = new StreamBuildListener(logOutput, StandardCharsets.UTF_8);

        factory.getParameters(build, listener);

        String log = logOutput.toString(StandardCharsets.UTF_8);

        // Verify the exception was caught and handled:
        assertTrue(
                log.contains("MacroEvaluationException") || log.contains("Exception"),
                "Log should contain exception information");

        // 2. Verify the original label was used (as set in the catch block)
        assertTrue(
                log.contains("Getting all nodes with label: " + malformedMacro),
                "Log should show the original label was used");
    }

    @Test
    void testGetParametersWithOfflineNode() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("projectWithOfflineNode");

        // Setup log capture to verify exception handling
        String nodeName = "offlineNode";
        String nodeLabel = "offlineLabel";
        createDumbSlaveNode(nodeName, nodeLabel, true);

        ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
        BuildListener listener = new StreamBuildListener(logOutput, StandardCharsets.UTF_8);

        AllNodesForLabelBuildParameterFactory factory =
                new AllNodesForLabelBuildParameterFactory("LABEL", nodeLabel, true);
        AbstractBuild<?, ?> build = project.scheduleBuild2(0).get();

        List<AbstractBuildParameters> parameters = factory.getParameters(build, listener);

        // Verify results
        assertFalse(parameters.isEmpty(), "Parameters list should not be empty");

        String log = logOutput.toString(StandardCharsets.UTF_8);

        // assert skipping offline
        assertTrue(
                log.contains(Messages.NodeListBuildParameterFactory_skippOfflineNode(nodeName)),
                "Log should contain message about skipping offline node");
        // assert no online nodes found -> indicates params were empty
        assertTrue(log.contains(Messages.NodeListBuildParameterFactory_noOnlineNodeFound(factory.nodeLabel)));
    }

    @Test
    void testGetParametersWithOnlineNode() throws Exception {
        // Create a test project
        FreeStyleProject project = j.createFreeStyleProject("projectWithOnlineNode");

        // Create a slave node that will remain online
        String onlineNodeName = "onlineNode";
        String onlineNodeLabel = "onlineLabel";
        DumbSlave node = createDumbSlaveNode(onlineNodeName, onlineNodeLabel, false);

        // Verify the node is online
        assertTrue(node.toComputer().isOnline(), "Node should be online for this test");

        AllNodesForLabelBuildParameterFactory factory =
                new AllNodesForLabelBuildParameterFactory("LABEL", onlineNodeLabel, true);

        AbstractBuild<?, ?> build = project.scheduleBuild2(0).get();
        ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
        BuildListener listener = new StreamBuildListener(logOutput, StandardCharsets.UTF_8);

        // Get parameters
        List<AbstractBuildParameters> parameters = factory.getParameters(build, listener);

        // Verify results
        assertFalse(parameters.isEmpty(), "Parameters list should not be empty");

        // Check if the online node is included in parameters
        boolean foundOnlineNode = false;
        for (AbstractBuildParameters param : parameters) {
            if (param instanceof NodeLabelBuildParameter) {
                String nodeLabel = ((NodeLabelBuildParameter) param).nodeLabel;
                if (nodeLabel.equals(onlineNodeName)) {
                    foundOnlineNode = true;
                    break;
                }
            }
        }

        assertTrue(foundOnlineNode, "Online node should be included in parameters");

        // Get the log to verify messaging
        String log = logOutput.toString(StandardCharsets.UTF_8);

        assertTrue(log.contains("Found nodes:"), "Log should indicate nodes were found");
    }

    private DumbSlave createDumbSlaveNode(String nodeName, String nodeLabel, Boolean isOffline) throws Exception {
        DumbSlave node = j.createSlave(nodeName, nodeLabel, new EnvVars());
        node.toComputer().setTemporarilyOffline(isOffline, null);
        if (!isOffline) {
            j.waitOnline(node);
        }
        return node;
    }

    private static void noMatchingNodeShouldYieldSameLabel(
            final AllNodesForLabelBuildParameterFactory dummyNodesFactory,
            AbstractBuild<?, ?> build,
            BuildListener listener)
            throws IOException, InterruptedException, DontTriggerException {
        List<AbstractBuildParameters> parameters = dummyNodesFactory.getParameters(build, listener);
        Set<String> nodeNames = new HashSet<>();
        for (AbstractBuildParameters parameter : parameters) {
            nodeNames.add(((NodeLabelBuildParameter) parameter).nodeLabel);
        }
        assertThat(nodeNames, contains("dummy"));
    }

    private static void shouldGetParameterForEachMatchingNode(
            final AllNodesForLabelBuildParameterFactory twoNodesFactory,
            AbstractBuild<?, ?> build,
            BuildListener listener)
            throws IOException, InterruptedException, DontTriggerException {
        // test: shouldGetParameterForEachMatchingNode
        List<AbstractBuildParameters> parameters = twoNodesFactory.getParameters(build, listener);
        Set<String> nodeNames = new HashSet<>();
        for (AbstractBuildParameters parameter : parameters) {
            nodeNames.add(((NodeLabelBuildParameter) parameter).nodeLabel);
        }
        assertThat(nodeNames, contains("node2", "node3"));
    }

    private static TriggerBuilder createTriggerBuilder(
            AbstractProject<?, ?> project, AbstractBuildParameterFactory factory) {
        return new TriggerBuilder(new BlockableBuildTriggerConfig(
                project.getName(),
                new BlockingBehaviour(Result.FAILURE, Result.UNSTABLE, Result.FAILURE),
                Collections.singletonList(factory),
                Collections.emptyList()));
    }
}

package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactory;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters.DontTriggerException;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BlockingBehaviour;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.slaves.DumbSlave;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

public class AllNodesForLabelBuildParameterFactoryUnitTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
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
    public void labelParameterFactoriesMustOnlyCreateValidParameters() throws Exception {

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
                    Assert.fail(e.getMessage());
                    return false;
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
    public void testGetDisplayName() {
        AllNodesForLabelBuildParameterFactory.DescriptorImpl descriptor =
                new AllNodesForLabelBuildParameterFactory.DescriptorImpl();

        assertEquals("All Nodes for Label Factory", descriptor.getDisplayName());
    }

    @Test
    public void testIsIgnoreOfflineNodes() {
        AllNodesForLabelBuildParameterFactory factory =
                new AllNodesForLabelBuildParameterFactory("name", "label", true);

        // Assert that the ignoreOfflineNodes flag is set to true
        assertEquals(true, factory.isIgnoreOfflineNodes());

        factory = new AllNodesForLabelBuildParameterFactory("name", "label", false);

        // Assert that the ignoreOfflineNodes flag is set to false
        assertEquals(false, factory.isIgnoreOfflineNodes());
    }

    public static void noMatchingNodeShouldYieldSameLabel(
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

    public static void shouldGetParameterForEachMatchingNode(
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

    private TriggerBuilder createTriggerBuilder(AbstractProject<?, ?> project, AbstractBuildParameterFactory factory) {
        TriggerBuilder tBuilder = new TriggerBuilder(new BlockableBuildTriggerConfig(
                project.getName(),
                new BlockingBehaviour(Result.FAILURE, Result.UNSTABLE, Result.FAILURE),
                Collections.singletonList(factory),
                Collections.emptyList()));
        return tBuilder;
    }
}

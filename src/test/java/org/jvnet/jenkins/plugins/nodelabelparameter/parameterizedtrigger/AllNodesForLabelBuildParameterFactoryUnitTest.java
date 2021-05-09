package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import static org.assertj.core.api.Assertions.assertThat;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
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
     * This test case is checks different behaviors of the {@link AllNodesForLabelBuildParameterFactory}, these are combined into one, otherwise #oMatchingNodeShouldYieldSameLabel would stuck in the
     * queue and the test would never finish.
     * 
     * @throws Exception
     */
    @Test
    public void labelParameterFactoresMustOnlyCreateValidParameters() throws Exception {

        final AllNodesForLabelBuildParameterFactory twoNodesFactory = new AllNodesForLabelBuildParameterFactory("LABEL", "label", false);
        final AllNodesForLabelBuildParameterFactory dummyNodesFactory = new AllNodesForLabelBuildParameterFactory("LABEL", "dummy", false);

        FreeStyleProject projectA = j.createFreeStyleProject("projectA");
        FreeStyleProject projectB = j.createFreeStyleProject("projectB");

        final List<Boolean> executed = new ArrayList<Boolean>();

        projectA.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
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
        j.assertBuildStatus(Result.SUCCESS, projectA.scheduleBuild2(0, new Cause.UserIdCause()).get());
        // make sure the test was really executed
        assertThat(executed).containsOnly(Boolean.TRUE);

    }

    public static void noMatchingNodeShouldYieldSameLabel(final AllNodesForLabelBuildParameterFactory dummyNodesFactory, AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException, DontTriggerException {
        List<AbstractBuildParameters> parameters = dummyNodesFactory.getParameters(build, listener);
        Set<String> nodeNames = new HashSet<String>();
        for (AbstractBuildParameters parameter : parameters) {
            nodeNames.add(((NodeLabelBuildParameter) parameter).nodeLabel);
        }
        assertThat(nodeNames).containsOnly("dummy");
    }

    public static void shouldGetParameterForEachMatchingNode(final AllNodesForLabelBuildParameterFactory twoNodesFactory, AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException, DontTriggerException {
        // test: shouldGetParameterForEachMatchingNode
        List<AbstractBuildParameters> parameters = twoNodesFactory.getParameters(build, listener);
        Set<String> nodeNames = new HashSet<String>();
        for (AbstractBuildParameters parameter : parameters) {
            nodeNames.add(((NodeLabelBuildParameter) parameter).nodeLabel);
        }
        assertThat(nodeNames).containsOnly("node2", "node3");
    }

    private TriggerBuilder createTriggerBuilder(AbstractProject<?, ?> project, AbstractBuildParameterFactory factory) {
        TriggerBuilder tBuilder = new TriggerBuilder(new BlockableBuildTriggerConfig(project.getName(), new BlockingBehaviour(Result.FAILURE, Result.UNSTABLE, Result.FAILURE), Collections.singletonList(factory),
                Collections.<AbstractBuildParameters> emptyList()));
        return tBuilder;
    }
}

package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.*;
import hudson.plugins.parameterizedtrigger.*;
import hudson.slaves.DumbSlave;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.*;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

public class NodeListBuildParameterFactoryTest {
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
    public void testNodeListBuildParameterFactoryConstructor() throws Exception {

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

                    NodeListBuildParameterFactory factory =
                            new NodeListBuildParameterFactory("labelName", "nodeListName");
                    List<AbstractBuildParameters> parameters = factory.getParameters(build, listener);
                    Set<String> nodeNames = new HashSet<>();
                    for (AbstractBuildParameters parameter : parameters) {
                        nodeNames.add(((NodeLabelBuildParameter) parameter).nodeLabel);
                    }

                    final NodeListBuildParameterFactory.DescriptorImpl descriptor =
                            new NodeListBuildParameterFactory.DescriptorImpl();
                    final AutoCompletionCandidates candidates = descriptor.doAutoCompleteNodeListString("");
                    Item parent1 = j.jenkins.getItem("projectA");
                    Item parent2 = projectA.asItem();

                    final FormValidation errorValue = descriptor.doCheckNodeListString(parent1, "");
                    final FormValidation okValue = descriptor.doCheckNodeListString(parent2, "node1");

                    Assert.assertEquals(errorValue.kind, FormValidation.Kind.ERROR);
                    Assertions.assertThat(nodeNames).containsOnly("nodeListName");
                    Assert.assertEquals(candidates.getValues(), List.of("node1", "node2", "node3"));
                    assertThat(factory.name, is("labelName"));
                    assertThat(factory.nodeListString, is("nodeListName"));

                } catch (AbstractBuildParameters.DontTriggerException e) {
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
        Assertions.assertThat(executed).containsOnly(Boolean.TRUE);
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

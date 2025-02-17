package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.*;
import hudson.plugins.parameterizedtrigger.*;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class NodeListBuildParameterFactoryTest {

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule j) throws Exception {
        this.j = j;
        j.createSlave("node1", "otherlabel", new EnvVars());
        j.createSlave("node2", "label", new EnvVars());
        j.createSlave("node3", "label", new EnvVars());
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
    void testNodeListBuildParameterFactoryConstructor() throws Exception {

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

                    assertEquals(FormValidation.Kind.ERROR, errorValue.kind);
                    assertThat(nodeNames, contains("nodeListName"));
                    assertEquals(candidates.getValues(), List.of("node1", "node2", "node3"));
                    assertThat(factory.name, is("labelName"));
                    assertThat(factory.nodeListString, is("nodeListName"));

                } catch (AbstractBuildParameters.DontTriggerException e) {
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
    void testDoCheckNodeListString() throws Exception {
        NodeListBuildParameterFactory.DescriptorImpl descriptor = new NodeListBuildParameterFactory.DescriptorImpl();

        FreeStyleProject projectA = j.createFreeStyleProject("projectA");

        Item parent1 = j.jenkins.getItem("projectA");
        Item parent2 = projectA.asItem();

        // Validate the FormValidation.ok() case
        FormValidation okValidation = descriptor.doCheckNodeListString(parent2, "node1");

        assertEquals(FormValidation.Kind.OK, okValidation.kind);

        // Validate the FormValidation.error() case if node is null
        FormValidation errorValidation = descriptor.doCheckNodeListString(parent1, null);

        assertEquals(FormValidation.Kind.ERROR, errorValidation.kind);
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

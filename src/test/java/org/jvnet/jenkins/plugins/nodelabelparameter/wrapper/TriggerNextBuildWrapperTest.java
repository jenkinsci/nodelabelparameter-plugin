package org.jvnet.jenkins.plugins.nodelabelparameter.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.Extension;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.slaves.DumbSlave;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.jenkins.plugins.nodelabelparameter.Constants;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterDefinition;
import org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterValue;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;

@WithJenkins
public class TriggerNextBuildWrapperTest {

    @Test
    void testTriggerBuildsConcurrent(JenkinsRule j) throws Exception {
        // Create multiple slaves
        String labelPrefix = "concurrent-label-";
        DumbSlave slave1 = j.createSlave(labelPrefix + "1", null, null);
        DumbSlave slave2 = j.createSlave(labelPrefix + "2", null, null);
        DumbSlave slave3 = j.createSlave(labelPrefix + "3", null, null);

        // Wait for slaves to come online
        j.waitOnline(slave1);
        j.waitOnline(slave2);
        j.waitOnline(slave3);

        // Create project with concurrent builds enabled
        FreeStyleProject project = j.createFreeStyleProject("concurrent-project");
        project.setConcurrentBuild(true);

        // Create a label parameter with multiple nodes
        String paramName = "NODES";
        List<String> nodeLabels = new ArrayList<>();
        nodeLabels.add(labelPrefix + "1");
        nodeLabels.add(labelPrefix + "2");
        nodeLabels.add(labelPrefix + "3");

        LabelParameterDefinition labelParam = new LabelParameterDefinition(
                paramName, "description", nodeLabels.get(0), true, false, Constants.ALL_CASES);
        project.addProperty(new ParametersDefinitionProperty(labelParam));

        // Trigger a build with all three labels
        LabelParameterValue labelParamValue = new LabelParameterValue(paramName, nodeLabels, new AllNodeEligibility());
        FreeStyleBuild build =
                project.scheduleBuild2(0, new ParametersAction(labelParamValue)).get();

        j.assertBuildStatusSuccess(build);

        // Wait for all builds to complete
        j.waitUntilNoActivity();

        // There should be 3 total builds (original + 2 triggered)
        assertEquals(3, project.getBuilds().size());
    }

    @Test
    void testTriggerNextBuildWithFailure(JenkinsRule j) throws Exception {
        // Create multiple slaves
        String labelPrefix = "sequential-label-";
        DumbSlave slave1 = j.createSlave(labelPrefix + "1", null, null);
        DumbSlave slave2 = j.createSlave(labelPrefix + "2", null, null);

        // Wait for slaves to come online
        j.waitOnline(slave1);
        j.waitOnline(slave2);

        // Create project with sequential builds
        FreeStyleProject project = j.createFreeStyleProject("sequential-project");
        project.setConcurrentBuild(false);

        // Create a label parameter with multiple nodes
        String paramName = "NODES";
        List<String> nodeLabels = new ArrayList<>();
        nodeLabels.add(labelPrefix + "1");
        nodeLabels.add(labelPrefix + "2");

        // Set triggerIfResult to SUCCESS - if build fails, it should not trigger next
        // build
        LabelParameterDefinition labelParam = new LabelParameterDefinition(
                paramName, "description", nodeLabels.get(0), true, false, Constants.CASE_SUCCESS);
        project.addProperty(new ParametersDefinitionProperty(labelParam));

        // Add a build step that makes the build fail
        project.getBuildersList().add(new FailureBuilder());

        // Trigger a build with both labels
        LabelParameterValue labelParamValue = new LabelParameterValue(paramName, nodeLabels, new AllNodeEligibility());
        FreeStyleBuild build =
                project.scheduleBuild2(0, new ParametersAction(labelParamValue)).get();

        j.assertBuildStatus(Result.FAILURE, build);

        // Wait for all builds to complete
        j.waitUntilNoActivity();

        // There should be only 1 build since the build failed
        assertEquals(1, project.getBuilds().size());
    }

    /**
     * A builder that makes the build fail.
     */
    public static class FailureBuilder extends Builder {
        @Override
        public boolean perform(
                hudson.model.AbstractBuild<?, ?> build, hudson.Launcher launcher, hudson.model.BuildListener listener) {
            build.setResult(Result.FAILURE);
            return false;
        }

        @Extension
        public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
            @Override
            public boolean isApplicable(Class jobType) {
                return true;
            }

            @Override
            public String getDisplayName() {
                return "Make build fail";
            }
        }
    }
}

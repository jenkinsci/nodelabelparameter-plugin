package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class NodeListBuildParameterFactoryRuleTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    void testIsListParameterBlank() {
        String nodeListStringExpanded = "nodeListName";
    }
}

package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class NodeListBuildParameterFactoryTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    void testNodeListBuildParameterFactoryConstructor() {
        NodeListBuildParameterFactory factory = new NodeListBuildParameterFactory("labelName", "nodeListName");
        assertThat(factory.name, is("labelName"));
        assertThat(factory.nodeListString, is("nodeListName"));
    }

    @Test
    void testIsListParameterBlank() {

        String nodeListStringExpanded = "nodeListName";        

    }
}

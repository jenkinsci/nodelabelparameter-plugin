package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class NodeListBuildParameterFactoryTest {
    @Test
    void testNodeListBuildParameterFactoryConstructor() {
        NodeListBuildParameterFactory factory =
                new NodeListBuildParameterFactory("labelName", "nodeListName");
        assertThat(factory.name, is("labelName"));
        assertThat(factory.nodeListString, is("nodeListName"));
    }
}

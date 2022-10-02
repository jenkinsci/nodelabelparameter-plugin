package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class NodeListBuildParameterFactoryTest {
    @Test
    void testNodeListBuildParameterFactoryConstructor() {
        NodeListBuildParameterFactory factory = new NodeListBuildParameterFactory("labelName", "nodeListName");
        assertThat(factory.name, is("labelName"));
        assertThat(factory.nodeListString, is("nodeListName"));
    }
}

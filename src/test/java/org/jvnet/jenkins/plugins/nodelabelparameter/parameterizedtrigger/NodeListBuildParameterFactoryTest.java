package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import org.junit.jupiter.api.Test;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;


import static org.hamcrest.Matchers.is;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;


public class NodeListBuildParameterFactoryTest {
    @Test
    void testNodeListBuildParameterFactoryConstructor() {
        NodeListBuildParameterFactory factory = new NodeListBuildParameterFactory("labelName", "nodeListName");
        assertThat(factory.name, is("labelName"));
        assertThat(factory.nodeListString, is("nodeListName"));
    }

    @Test
    void testGetParameters() {
        AbstractBuild<?, ?> build = new AbstractBuild<?,?>() {
            
        };
        TaskListener listener;
        try {
            String labelExpanded = TokenMacro.expandAll(build, listener, labelExpanded);
        } catch (MacroEvaluationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}

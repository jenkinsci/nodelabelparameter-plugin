package org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger;

import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactory;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactoryDescriptor;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import jenkins.model.Jenkins;
import org.jvnet.jenkins.plugins.nodelabelparameter.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

/**
 * Triggers builds on all slaves.
 *
 * @author Kohsuke Kawaguchi
 */
public class AllNodesBuildParameterFactory extends AbstractBuildParameterFactory {
    @DataBoundConstructor
    public AllNodesBuildParameterFactory() {
    }

    @Override
    public List<AbstractBuildParameters> getParameters(AbstractBuild<?, ?> build, TaskListener listener) {
        Computer[] nodes = Jenkins.getInstance().getComputers();

        List<AbstractBuildParameters> params = Lists.newArrayList();
        for(Computer c : nodes) {
            Node n = c.getNode();
            if (n!=null && c.isOnline() && c.getNumExecutors()>0)
                params.add(new NodeLabelBuildParameter("label",
                        n.getSelfLabel().getName()));
        }

		return params;
    }

    // Dependency to parameterized trigger is optional, so this is marked optional
    @Extension(optional = true)
    public static class DescriptorImpl extends AbstractBuildParameterFactoryDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.AllNodesBuildParameterFactory_displayName();
        }
    }
}

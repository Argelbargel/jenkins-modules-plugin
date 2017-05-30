package argelbargel.jenkins.plugins.modules;


import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static argelbargel.jenkins.plugins.modules.ModuleUtils.buildDownstream;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.buildUpstream;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.findModule;


public final class UpstreamDependencyDescribable extends AbstractDescribableImpl<UpstreamDependencyDescribable> implements Serializable {
    static List<UpstreamDependencyDescribable> wrap(Set<String> names) {
        List<UpstreamDependencyDescribable> dependencies = new ArrayList<>(names.size());
        for (String name : names) {
            dependencies.add(new UpstreamDependencyDescribable(name));
        }

        return dependencies;
    }

    static Set<String> unwrap(List<UpstreamDependencyDescribable> dependencies) {
        Set<String> names = new LinkedHashSet<>(dependencies.size());
        for (UpstreamDependencyDescribable dependency : dependencies) {
            names.add(dependency.getName());
        }

        return names;
    }

    private final String name;

    @DataBoundConstructor
    @Restricted(NoExternalUse.class)
    public UpstreamDependencyDescribable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Job<?, ?> getJob() {
        ModuleAction module = ModuleAction.get(getName());
        return module != null ? module.getJob() : null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UpstreamDependencyDescribable)) {
            return false;
        }
        UpstreamDependencyDescribable module = (UpstreamDependencyDescribable) o;
        return Objects.equals(name, module.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


    @Extension
    public static class DependencyDescriptor extends Descriptor<UpstreamDependencyDescribable> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Dependency";
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // used by config.jelly
        public FormValidation doCheckName(@QueryParameter String name, @AncestorInPath Job context) {
            if (StringUtils.isBlank(name)) {
                return FormValidation.error("module name must not be blank");
            }

            if (buildUpstream(name).contains(findModule(context))) {
                return FormValidation.error("circular dependency between this module and " + name);
            }

            return FormValidation.ok();
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // used by config.jelly
        public ComboBoxModel doFillNameItems(@AncestorInPath Job context) {
            Set<String> names = new HashSet<>(ModuleUtils.allNames());

            String current = findModule(context);
            if (current != null) {
                names.remove(current);
                names.removeAll(buildDownstream(current));
                names.removeAll(buildUpstream(current));
            }

            return new ComboBoxModel(names);
        }
    }
}

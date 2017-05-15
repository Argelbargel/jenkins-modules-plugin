package argelbargel.jenkins.plugins.modules;


import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
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


public final class ModuleDependency extends AbstractDescribableImpl<ModuleDependency> implements Serializable {
    static List<ModuleDependency> wrap(Set<String> names) {
        List<ModuleDependency> dependencies = new ArrayList<>(names.size());
        for (String name : names) {
            dependencies.add(new ModuleDependency(name));
        }

        return dependencies;
    }

    static Set<String> unwrap(List<ModuleDependency> dependencies) {
        Set<String> names = new LinkedHashSet<>(dependencies.size());
        for (ModuleDependency dependency : dependencies) {
            names.add(dependency.getName());
        }

        return names;
    }

    private final String name;

    @DataBoundConstructor
    @Restricted(NoExternalUse.class)
    public ModuleDependency(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public AbstractProject getProject() {
        ModuleAction module = ModuleAction.get(getName());
        return module != null ? module.getProject() : null;
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
        if (!(o instanceof ModuleDependency)) {
            return false;
        }
        ModuleDependency module = (ModuleDependency) o;
        return Objects.equals(name, module.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


    @Extension
    public static class DependencyDescriptor extends Descriptor<ModuleDependency> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Dependency";
        }

        @Restricted(NoExternalUse.class)
        public FormValidation doCheckName(@QueryParameter String name, @AncestorInPath AbstractProject context) {
            if (StringUtils.isBlank(name)) {
                return FormValidation.error("module name must not be blank");
            }

            if (buildUpstream(name).contains(findModule(context))) {
                return FormValidation.error("circular dependency between this module and " + name);
            }

            return FormValidation.ok();
        }

        @Restricted(NoExternalUse.class)
        public ComboBoxModel doFillNameItems(@AncestorInPath AbstractProject context) {
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

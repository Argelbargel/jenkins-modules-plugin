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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static argelbargel.jenkins.plugins.modules.Registry.registry;


public final class ModuleDependency extends AbstractDescribableImpl<ModuleDependency> implements Dependable, Serializable {
    private final String name;

    @DataBoundConstructor
    @Restricted(NoExternalUse.class)
    public ModuleDependency(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AbstractProject getProject() {
        return registry().project(name);
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

            if (registry().upstream(name).contains(registry().module(context))) {
                return FormValidation.error("circular dependency between this module and " + name);
            }

            return FormValidation.ok();
        }

        @Restricted(NoExternalUse.class)
        public ComboBoxModel doFillNameItems(@AncestorInPath AbstractProject context) {
            Set<String> names = new HashSet<>(registry().names());

            String current = registry().module(context);
            if (current != null) {
                names.remove(current);
                names.removeAll(registry().downstream(current));
                names.removeAll(registry().upstream(current));
            }

            return new ComboBoxModel(names);
        }
    }
}

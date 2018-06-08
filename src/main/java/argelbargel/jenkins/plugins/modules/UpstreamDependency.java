package argelbargel.jenkins.plugins.modules;


import argelbargel.jenkins.plugins.modules.UpstreamPredicate.UpstreamPredicateDescriptor;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static argelbargel.jenkins.plugins.modules.ModuleAction.getModuleAction;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.buildDownstream;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.buildUpstream;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.findJobs;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;


@Symbol("upstreamDependency")
public final class UpstreamDependency extends AbstractDescribableImpl<UpstreamDependency> implements Serializable {
    static Set<String> names(Collection<UpstreamDependency> dependencies) {
        return dependencies.stream()
                .map(UpstreamDependency::getName)
                .collect(toSet());
    }

    private final String name;
    private Collection<UpstreamPredicate> predicates;

    @DataBoundConstructor
    @Restricted(NoExternalUse.class)
    public UpstreamDependency(String name) {
        this.name = name;
        predicates = emptySet();
    }

    public String getName() {
        return name;
    }

    public void setPredicates(Collection<UpstreamPredicate> predicates) {
        this.predicates = predicates;
    }

    public Collection<UpstreamPredicate> getPredicates() {
        return predicates;
    }

    @SuppressWarnings("WeakerAccess") // wird von config.jelly benötigt
    public Collection<Job<?, ?>> getJobs() {
        // TODO: das muss doch besser gehen...
        Collection<Predicate<Job<?, ?>>> p = new ArrayList<>(predicates);
        return findJobs(name, p.stream().reduce(Predicate::and).orElse(c -> true));
    }

    void addUpstreamDependencies(ModuleDependencyGraph graph, Job owner) {
        getJobs().forEach(j -> graph.addUpstreamDependency(j, owner));
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
        if (!(o instanceof UpstreamDependency)) {
            return false;
        }
        UpstreamDependency module = (UpstreamDependency) o;
        return Objects.equals(name, module.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


    @Extension
    public static class DependencyDescriptor extends Descriptor<UpstreamDependency> {
        @SuppressWarnings("unused") // used by config.jelly
        public List<UpstreamPredicateDescriptor> getPredicateDescriptors() {
            return UpstreamPredicateDescriptor.getAllUpstreamPredicateDescriptors();
        }

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

            if (buildUpstream(name).contains(getModuleAction(context).getModuleName())) {
                return FormValidation.error("circular dependency between this module and " + name);
            }

            return FormValidation.ok();
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // used by config.jelly
        public ComboBoxModel doFillNameItems(@AncestorInPath Job context) {
            Set<String> names = new HashSet<>(ModuleUtils.allModuleNames());

            String current = getModuleAction(context).getModuleName();
            if (current != null) {
                names.remove(current);
                names.removeAll(buildDownstream(current));
                names.removeAll(buildUpstream(current));
            }

            return new ComboBoxModel(names);
        }
    }
}

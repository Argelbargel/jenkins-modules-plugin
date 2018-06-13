package argelbargel.jenkins.plugins.modules.upstream;


import argelbargel.jenkins.plugins.modules.ModuleAction;
import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph;
import argelbargel.jenkins.plugins.modules.ModuleUtils;
import argelbargel.jenkins.plugins.modules.upstream.predicates.AndUpstreamPredicate;
import argelbargel.jenkins.plugins.modules.upstream.predicates.UpstreamPredicate;
import argelbargel.jenkins.plugins.modules.upstream.predicates.UpstreamPredicate.UpstreamPredicateDescriptor;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static argelbargel.jenkins.plugins.modules.ModuleAction.getModuleAction;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.findJobs;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;


@Symbol("upstreamDependency")
public final class UpstreamDependency extends AbstractDescribableImpl<UpstreamDependency> implements Serializable {
    public static Set<String> upstreamDependencyNames(Collection<UpstreamDependency> dependencies) {
        return dependencies.stream()
                .map(UpstreamDependency::getName)
                .collect(toSet());
    }

    private String name;
    private List<UpstreamPredicate> predicates;

    @Restricted(NoExternalUse.class)
    public UpstreamDependency(String name) {
        this(name, emptyList());
    }

    @DataBoundConstructor
    @Restricted(NoExternalUse.class)
    public UpstreamDependency(String name, List<UpstreamPredicate> predicates) {
        this.name = name;
        this.predicates = predicates;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setPredicates(List<UpstreamPredicate> predicates) {
        this.predicates = predicates;
    }

    public Collection<UpstreamPredicate> getPredicates() {
        return predicates;
    }

    @SuppressWarnings("unused") // wird von config.jelly benötigt
    public Collection<Job<?, ?>> getJobs() {
        return findJobs(name).collect(toSet());
    }

    public void addUpstreamDependencies(ModuleDependencyGraph graph, Job owner) {
        UpstreamPredicate predicate = new AndUpstreamPredicate(predicates).reset();
        findJobs(name)
                .sorted((u1, u2) -> predicate.compare(u1, u2, owner))
                .filter(u -> predicate.test(u, owner))
                .forEach(u -> graph.addUpstreamDependency(u, owner));
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
    @Symbol("upstreamDependency")
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

            if (ModuleUtils.buildUpstream(name).contains(name)) {
                return FormValidation.error("circular dependency between this module and " + name);
            }

            return FormValidation.ok();
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // used by config.jelly
        public ComboBoxModel doFillNameItems(@AncestorInPath Job context) {
            Set<String> names = new HashSet<>(ModuleUtils.allModuleNames());

            Optional<String> current = ofNullable(getModuleAction(context)).map(ModuleAction::getModuleName);
            if (current.isPresent()) {
                names.remove(current.get());
                names.removeAll(ModuleUtils.buildDownstream(current.get()));
                names.removeAll(ModuleUtils.buildUpstream(current.get()));
            }

            return new ComboBoxModel(names);
        }
    }
}

package argelbargel.jenkins.plugins.modules;


import hudson.model.Job;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static argelbargel.jenkins.plugins.modules.ModuleAction.getModuleAction;
import static argelbargel.jenkins.plugins.modules.UpstreamDependency.names;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;


final class ModuleUtils {
    static Set<String> allModuleNames() {
        Set<String> names = new HashSet<>();
        all().forEach(module -> {
                    names.add(module.getModuleName());
                    names.addAll(module.getUpstreamDependencies().stream().map(UpstreamDependency::getName).collect(toSet()));
                }
        );
        return names;
    }

    static Set<String> allModuleNamesWithJobs() {
        return all().map(ModuleAction::getModuleName).collect(toSet());
    }

    static Collection<Job<?, ?>> findJobs(String name, Predicate<Job<?, ?>> predicate) {
        return withName(name).map(ModuleAction::getJob).filter(predicate).collect(toSet());
    }

    static Collection<Job<?, ?>> findJobs(String name) {
        return findJobs(name, j -> true);
    }

    static Set<String> buildDownstream(String name) {
        Set<String> upstream = new HashSet<>();
        buildDownstream(upstream, name);
        return upstream;
    }

    private static Stream<ModuleAction> all() {
        return Jenkins.get().getAllItems(Job.class).stream()
                .map(j -> ofNullable(getModuleAction(j)))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private static Stream<ModuleAction> withName(@Nonnull String name) {
        return all().filter(m -> name.equals(m.getModuleName()));
    }

    private static void buildDownstream(Set<String> downstream, String name) {
        withName(name)
                .filter(m -> !downstream.contains(m.getModuleName()) && names(m.getUpstreamDependencies()).contains(name))
                .map(ModuleAction::getModuleName)
                .forEach(n -> {
                    downstream.add(n);
                    buildDownstream(downstream, n);
                });
    }

    static Set<String> buildUpstream(String name) {
        Set<String> upstream = new HashSet<>();
        buildUpstream(upstream, name);
        return upstream;
    }

    private static void buildUpstream(Set<String> upstream, String name) {
        withName(name)
                .flatMap(m -> m.getUpstreamDependencies().stream())
                .map(UpstreamDependency::getName)
                .filter(d -> !upstream.contains(d))
                .forEach(d -> {
                    upstream.add(d);
                    buildUpstream(upstream, d);
                });
    }

    private ModuleUtils() { /* no instances allowed */ }

}

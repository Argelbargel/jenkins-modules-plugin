package argelbargel.jenkins.plugins.modules;


import argelbargel.jenkins.plugins.modules.queue.predicates.AndQueuePredicate;
import argelbargel.jenkins.plugins.modules.queue.predicates.QueuePredicate;
import argelbargel.jenkins.plugins.modules.upstream.UpstreamDependency;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import hudson.model.Actionable;
import hudson.model.InvisibleAction;
import hudson.model.Job;
import hudson.util.XStream2;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static argelbargel.jenkins.plugins.modules.ModuleUtils.buildUpstream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang.StringUtils.isNotBlank;


public final class ModuleAction extends InvisibleAction {
    public static ModuleAction getModuleAction(@Nonnull Actionable actionable) {
        return actionable.getAction(ModuleAction.class);
    }

    private final String name;
    private Set<UpstreamDependency> upstreamDependencies;
    @Deprecated
    private Set<String> dependencies;
    private List<QueuePredicate> predicates;
    private long waitInterval;


    ModuleAction(String name) {
        this.name = name;
        this.upstreamDependencies = emptySet();
        this.predicates = emptyList();
        this.waitInterval = 0;
    }

    public String getModuleName() {
        return name;
    }

    public QueuePredicate getPredicate() {
        return new AndQueuePredicate(predicates);
    }

    @Nonnull
    public Job<?, ?> getJob() {
        return Jenkins.get().getAllItems(Job.class).stream()
                .filter(j -> ofNullable(getModuleAction(j)).filter(this::equals).isPresent())
                .findFirst().orElseThrow(IllegalStateException::new);
    }

    @SuppressWarnings("unused") // used by jobMain.jelly
    public List<Job> getUpstreamJobs() {
        return ModuleDependencyGraph.get().getUpstream(getJob());
    }

    @SuppressWarnings("unused") // used by jobMain.jelly
    public List<Job> getDownstreamJobs() {
        return ModuleDependencyGraph.get().getDownstream(getJob());
    }

    public long getDependencyWaitInterval() {
        return waitInterval;
    }

    void addUpstreamDependencies(ModuleDependencyGraph graph, Job owner) {
        upstreamDependencies.forEach(d -> d.addUpstreamDependencies(graph, owner));
    }

    Set<UpstreamDependency> getUpstreamDependencies() {
        return upstreamDependencies;
    }

    void setUpstreamDependencies(Collection<UpstreamDependency> dependencies) {
        this.upstreamDependencies = dependencies.stream()
                .filter(d -> isNotBlank(d.getName()) && !buildUpstream(d.getName()).contains(name))
                .collect(toSet());
    }

    List<QueuePredicate> getQueuePredicates() {
        return predicates;
    }

    void setQueuePredicates(List<QueuePredicate> predicates) {
        this.predicates = predicates;
    }

    void setDependencyWaitInterval(long millisecs) {
        waitInterval = millisecs;
    }


    @Deprecated // >= 0.9.1
    @SuppressWarnings({"deprecation", "unused"})
    public static final class ConverterImpl extends XStream2.PassthruConverter<ModuleAction> {
        public ConverterImpl(XStream2 xstream) {
            super(xstream);
        }

        @Override
        protected void callback(ModuleAction obj, UnmarshallingContext context) {
            if (obj.dependencies != null) {
                obj.upstreamDependencies = wrap(obj.dependencies);
                obj.dependencies = null;
            }
        }

        @Deprecated
        private Set<UpstreamDependency> wrap(Set<String> names) {
            return names.stream()
                    .map(UpstreamDependency::new)
                    .collect(toSet());
        }
    }

}

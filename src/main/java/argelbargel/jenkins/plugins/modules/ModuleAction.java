package argelbargel.jenkins.plugins.modules;


import argelbargel.jenkins.plugins.modules.parameters.TriggerParameter;
import argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate;
import argelbargel.jenkins.plugins.modules.predicates.AndActionsPredicate;
import hudson.model.Actionable;
import hudson.model.InvisibleAction;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.Result;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static argelbargel.jenkins.plugins.modules.ModuleUtils.buildUpstream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;


public final class ModuleAction extends InvisibleAction {
    public static ModuleAction get(@Nonnull Actionable actionable) {
        return actionable.getAction(ModuleAction.class);
    }

    public static ModuleAction get(@Nonnull String name) {
        for (ModuleAction module : all()) {
            if (name.equals(module.getModuleName())) {
                return module;
            }
        }

        return null;
    }

    static Set<ModuleAction> all() {
        Set<ModuleAction> all = new HashSet<>();
        for (Job<?, ?> job : Jenkins.getInstance().getAllItems(Job.class)) {
            ModuleAction module = job.getAction(ModuleAction.class);
            if (module != null) {
                all.add(module);
            }
        }

        return all;
    }

    private final String name;
    private Set<String> dependencies;
    private List<ActionsPredicate> predicates;
    private long waitInterval;
    private Result triggerResult;
    private boolean triggerDownstreamWithCurrentParameters;
    private List<TriggerParameter> triggerParameters;


    ModuleAction(String name) {
        this.name = name;
        this.dependencies = emptySet();
        this.predicates = emptyList();
        this.waitInterval = 0;
        this.triggerResult = Result.SUCCESS;
        this.triggerDownstreamWithCurrentParameters = true;
        this.triggerParameters = emptyList();
    }

    public String getModuleName() {
        return name;
    }

    public ActionsPredicate getPredicate() {
        return new AndActionsPredicate(predicates);
    }

    public Job<?, ?> getJob() {
        for (Job<?, ?> job : Jenkins.getInstance().getAllItems(Job.class)) {
            if (equals(job.getAction(ModuleAction.class))) {
                return job;
            }
        }

        return null;
    }

    @SuppressWarnings("unused") // used by jobMain.jelly
    public List<Job> getUpstreamJobs() {
        return ModuleDependencyGraph.get().getUpstream(getJob());
    }

    @SuppressWarnings("unused") // used by jobMain.jelly
    public List<Job> getDownstreamJobs() {
        return ModuleDependencyGraph.get().getDownstream(getJob());
    }

    @Restricted(NoExternalUse.class)
    @SuppressWarnings("unused") // used by jobMain.jelly
    public String moduleName(Job job) {
        return ModuleAction.get(job).getModuleName();
    }

    public long getDependencyWaitInterval() {
        return waitInterval;
    }

    public boolean mustCancelDownstream(Result result) {
        return result.isWorseThan(triggerResult);
    }

    public boolean getTriggerDownstreamWithCurrentParameters() {
        return triggerDownstreamWithCurrentParameters;
    }

    boolean shouldTriggerDownstream(Result result, ParametersAction parameters) {
        return result.isBetterOrEqualTo(triggerResult) && shouldTriggerDownstream(parameters);
    }

    private boolean shouldTriggerDownstream(ParametersAction parameters) {
        for (TriggerParameter t : triggerParameters) {
            if (!t.test(parameters)) {
                return false;
            }
        }

        return true;
    }

    Set<String> getDependencies() {
        return dependencies;
    }

    void setDependencies(Set<String> dependencies) {
        this.dependencies = new HashSet<>(dependencies.size());
        for (String dependency : dependencies) {
            if (StringUtils.isNotBlank(dependency) && !buildUpstream(dependency).contains(name)) {
                this.dependencies.add(dependency);
            }
        }
    }

    List<ActionsPredicate> getPredicates() {
        return predicates;
    }

    void setPredicates(List<ActionsPredicate> predicates) {
        this.predicates = predicates;
    }

    void setDependencyWaitInterval(long millisecs) {
        waitInterval = millisecs;
    }

    Result getTriggerResult() {
        return triggerResult;
    }

    void setTriggerResult(Result result) {
        triggerResult = result;
    }

    void setTriggerDownstreamWithCurrentParameters(boolean value) {
        triggerDownstreamWithCurrentParameters = value;
    }

    void setTriggerParameters(List<TriggerParameter> parameters) {
        triggerParameters = parameters;
    }

    List<TriggerParameter> getTriggerParameters() {
        return triggerParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModuleAction)) {
            return false;
        }
        ModuleAction that = (ModuleAction) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

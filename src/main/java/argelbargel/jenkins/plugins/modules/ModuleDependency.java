package argelbargel.jenkins.plugins.modules;


import argelbargel.jenkins.plugins.modules.parameters.TriggerParameter;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;

import java.util.List;
import java.util.Set;


public final class ModuleDependency {
    private final Result triggerResult;
    private final List<TriggerParameter> triggerParameters;
    private final Job upstream;
    private final Job downstream;
    private final boolean triggerBuildWithCurrentParameters;

    ModuleDependency(Job<?, ?> upstream, Job<?, ?> downstream, Result result, List<TriggerParameter> parameters, boolean triggerWithCurrentParameters) {
        this.upstream = upstream;
        this.downstream = downstream;
        this.triggerBuildWithCurrentParameters = triggerWithCurrentParameters;
        this.triggerResult = result;
        this.triggerParameters = parameters;
    }

    public boolean shouldTriggerBuild(Run<?, ?> build) {
        return shouldTriggerDownstream(build.getResult(), build.getAction(ParametersAction.class)) && willNotBlock();
    }

    private boolean shouldTriggerDownstream(Result result, ParametersAction parameters) {
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

    @SuppressWarnings("unchecked")
    private boolean willNotBlock() {
        List<Job> downstream = ModuleDependencyGraph.get().getDownstream(getUpstreamJob());
        Set<Job> upstream = ModuleDependencyGraph.get().getTransitiveUpstream(getDownstreamJob());
        upstream.remove(getUpstreamJob());
        downstream.retainAll(upstream);
        return downstream.isEmpty();
    }

    @SuppressWarnings("WeakerAccess") // part of public API
    public final Job getUpstreamJob() {
        return upstream;
    }

    public final Job getDownstreamJob() {
        return downstream;
    }

    public final boolean shouldTriggerBuildWithCurrentParameters() {
        return triggerBuildWithCurrentParameters;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final ModuleDependency that = (ModuleDependency) obj;
        return this.upstream == that.upstream || this.downstream == that.downstream;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.upstream.hashCode();
        hash = 23 * hash + this.downstream.hashCode();
        return hash;
    }

    @Override
    public final String toString() {
        return super.toString() + "[" + upstream + "->" + downstream + "]";
    }
}

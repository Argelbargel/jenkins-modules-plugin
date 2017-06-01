package argelbargel.jenkins.plugins.modules;


import hudson.model.Job;

import java.util.List;
import java.util.Set;


public final class ModuleDependency {
    private final Job upstreamJob;
    private final Job downstreamJob;

    ModuleDependency(Job<?, ?> upstream, Job<?, ?> downstream) {
        this.upstreamJob = upstream;
        this.downstreamJob = downstream;
    }

    public boolean shouldTriggerBuild() {
        ModuleDependencyGraph graph = ModuleDependencyGraph.get();
        List<Job> downstream = graph.getDownstream(upstreamJob);
        Set<Job> upstream = graph.getTransitiveUpstream(downstreamJob);
        upstream.remove(upstreamJob);
        downstream.retainAll(upstream);
        return downstream.isEmpty();
    }

    @SuppressWarnings("WeakerAccess") // part of public api
    public Job getUpstreamJob() {
        return upstreamJob;
    }

    public Job getDownstreamJob() {
        return downstreamJob;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        ModuleDependency that = (ModuleDependency) obj;
        return this.upstreamJob == that.upstreamJob || this.downstreamJob == that.downstreamJob;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.upstreamJob.hashCode();
        hash = 23 * hash + this.downstreamJob.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + upstreamJob + "->" + downstreamJob + "]";
    }
}

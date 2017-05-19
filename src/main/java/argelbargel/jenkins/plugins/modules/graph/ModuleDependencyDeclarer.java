package argelbargel.jenkins.plugins.modules.graph;


import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph;
import argelbargel.jenkins.plugins.modules.queue.ModuleBlockedAction;
import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Job;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;


final class ModuleDependencyDeclarer {
    static Set<Run> findRoots(Run run) {
        Set<Run> roots = new HashSet<>();
        if (run != null) {
            findRoots(roots, run);
        }
        return roots;
    }

    private static void findRoots(Set<Run> roots, Run<?, ?> run) {
        boolean isRoot = true;
        for (Cause cause : run.getCauses()) {
            if (cause instanceof UpstreamCause) {
                isRoot = false;
                findRoots(roots, ((UpstreamCause) cause).getUpstreamRun());
            }
        }

        if (isRoot) {
            roots.add(run);
        }
    }

    @SuppressWarnings("unchecked")
    static List<Run> getDownstream(Run<?, ?> run) throws ExecutionException, InterruptedException {
        List<Run> runs = new ArrayList<>();
        for (Job downstream : ModuleDependencyGraph.get().getDownstream(run.getParent())) {
            addTriggeredAndBlockedBuilds(runs, (List<Run<?, ?>>) downstream.getBuilds(), run);
        }

        return runs;
    }

    private static void addTriggeredAndBlockedBuilds(List<Run> runs, List<Run<?, ?>> downstream, Run run) {
        for (Run<?, ?> d : downstream) {
            if (wasTriggeredBy(d, run) || hasRunFormerlyBlockedBy(d, run)) {
                runs.add(d);
            }
        }
    }

    private static boolean hasRunFormerlyBlockedBy(Run<?, ?> downstream, Run run) {
        ModuleBlockedAction blocked = ModuleBlockedAction.get(downstream);
        return blocked != null && blocked.wasBlockedBy(run);
    }

    private static boolean wasTriggeredBy(Run<?, ?> downstream, Run run) {
        for (Cause cause : downstream.getCauses()) {
            if (cause instanceof UpstreamCause && wasTriggeredBy((UpstreamCause) cause, run)) {
                return true;
            }
        }
        return false;
    }

    private static boolean wasTriggeredBy(UpstreamCause cause, Run run) {
        return cause.getUpstreamProject().equals(run.getParent().getFullName()) && cause.getUpstreamBuild() == run.getNumber();
    }

    private ModuleDependencyDeclarer() { /* no instances required */ }
}

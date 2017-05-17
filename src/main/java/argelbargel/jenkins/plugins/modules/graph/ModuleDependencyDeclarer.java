package argelbargel.jenkins.plugins.modules.graph;


import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph;
import argelbargel.jenkins.plugins.modules.queue.ModuleBlockedAction;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Job;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


class ModuleDependencyDeclarer {
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
            if (wasTriggeredBy(d, run) || wasBlockedBy(d, run)) {
                runs.add(d);
            }
        }
    }

    private static boolean wasBlockedBy(Run<?, ?> downstream, Run run) {
        ModuleBlockedAction blocked = ModuleBlockedAction.get(downstream);
        return blocked != null && blocked.hasBeenBlockedBy(run);
    }

    private static boolean wasTriggeredBy(Run<?, ?> downstream, Run run) {
        UpstreamCause cause = downstream.getCause(UpstreamCause.class);
        return cause != null && wasTriggeredBy(cause, run);
    }

    private static boolean wasTriggeredBy(UpstreamCause cause, Run run) {
        return cause.getUpstreamProject().equals(run.getParent().getFullName()) && cause.getUpstreamBuild() == run.getNumber();
    }
}

package argelbargel.jenkins.plugins.modules.buildgraphview;


import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph;
import argelbargel.jenkins.plugins.modules.queue.ModuleBlockedAction;
import hudson.Extension;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.plugins.buildgraphview.DownStreamRunDeclarer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


@Extension(optional = true)
public class ModuleDownstreamDeclarer extends DownStreamRunDeclarer {
    @SuppressWarnings("unchecked")
    @Override
    public List<Run> getDownStream(Run run) throws ExecutionException, InterruptedException {
        List<Run> runs = new ArrayList<>();
        for (Job downstream : ModuleDependencyGraph.get().getDownstream(run.getParent())) {
            addTriggeredAndBlockedBuilds(runs, (List<Run<?, ?>>) downstream.getBuilds(), run);
        }

        return runs;
    }

    private void addTriggeredAndBlockedBuilds(List<Run> runs, List<Run<?, ?>> downstream, Run run) {
        for (Run<?, ?> d : downstream) {
            if (wasTriggeredBy(d, run) || wasBlockedBy(d, run)) {
                runs.add(d);
            }
        }
    }

    private boolean wasBlockedBy(Run<?, ?> downstream, Run run) {
        ModuleBlockedAction blocked = ModuleBlockedAction.get(downstream);
        return blocked != null && blocked.hasBeenBlockedBy(run);
    }

    private boolean wasTriggeredBy(Run<?, ?> downstream, Run run) {
        UpstreamCause cause = downstream.getCause(UpstreamCause.class);
        return cause != null && wasTriggeredBy(cause, run);
    }

    private boolean wasTriggeredBy(UpstreamCause cause, Run run) {
        return cause.getUpstreamProject().equals(run.getParent().getFullName()) && cause.getUpstreamBuild() == run.getNumber();
    }
}

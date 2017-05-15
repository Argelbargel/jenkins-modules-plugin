package argelbargel.jenkins.plugins.modules.buildgraphview;


import argelbargel.jenkins.plugins.modules.queue.ModuleBlockedAction;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.plugins.buildgraphview.DownStreamRunDeclarer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


@Extension(optional = true)
public class ModuleDownstreamDeclarer extends DownStreamRunDeclarer {
    @Override
    public List<Run> getDownStream(Run r) throws ExecutionException, InterruptedException {
        Job job = r.getParent();
        return (job instanceof AbstractProject) ? getDownStream(r, (AbstractProject) r.getParent()) : Collections.<Run>emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Run> getDownStream(Run run, AbstractProject<?, ?> project) {
        List<Run> runs = new ArrayList<>();
        for (AbstractProject downstream : project.getDownstreamProjects()) {
            List<Run> builds = downstream.getBuilds();
            for (Run b : builds) {
                ModuleBlockedAction blocked = ModuleBlockedAction.get(b);
                if (blocked != null && blocked.hasBeenBlockedBy(run)) {
                    runs.add(b);
                }
            }
        }

        return runs;
    }
}

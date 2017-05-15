package argelbargel.jenkins.plugins.modules.queue;


import argelbargel.jenkins.plugins.modules.Messages;
import argelbargel.jenkins.plugins.modules.ModuleAction;
import argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate;
import com.google.common.base.Predicate;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Queue.Item;
import hudson.model.Run;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;
import hudson.util.RunList;
import jenkins.model.Jenkins;

import javax.annotation.Nullable;
import java.util.Collection;

import static argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate.find;


@Extension
public final class ModuleQueueDispatcher extends QueueTaskDispatcher {
    private static final int MINIMUM_WAIT_INTERVAL = 250;
    private static final int MILLIS_PER_SEC = 1000;

    private static final Predicate<Run<?, ?>> BUILDING_RUNS_PREDICATE = new Predicate<Run<?, ?>>() {
        @Override
        public boolean apply(@Nullable Run<?, ?> run) {
            return run != null && run.isBuilding();
        }
    };

    @Override
    public CauseOfBlockage canRun(Item waiting) {
        return waiting.task instanceof AbstractProject ? canRun(waiting, (AbstractProject) waiting.task) : super.canRun(waiting);
    }

    private CauseOfBlockage canRun(Item item, AbstractProject<?, ?> project) {
        ModuleAction module = ModuleAction.get(project);
        return module != null && !project.getUpstreamProjects().isEmpty() ? canRun(item, project, module) : null;
    }

    private CauseOfBlockage canRun(Item waiting, AbstractProject<?, ?> project, ModuleAction module) {
        long timeToWait = waitInterval(module) - waitedFor(waiting);
        if (timeToWait > 0) {
            return CauseOfBlockage.fromMessage(Messages._ModuleQueueDispatcher_waitForDependencies(Math.ceil(timeToWait / (float) MILLIS_PER_SEC)));
        }

        return canRun(waiting, project.getTransitiveUpstreamProjects(), module.getPredicate());
    }

    private CauseOfBlockage canRun(Item waiting, Collection<AbstractProject> upstream, ActionsPredicate predicate) {
        for (AbstractProject project : upstream) {
            Item item = find(predicate, waiting, Jenkins.getInstance().getQueue().getItems(project));
            if (item != null) {
                ModuleBlockedAction.block(waiting, item);
                return CauseOfBlockage.fromMessage(Messages._ModuleQueueDispatcher_blockedByUpstream(item.task.getDisplayName()));
            }

            Run<?, ?> run = find(predicate, waiting, getBuildingRuns(project));
            if (run != null) {
                ModuleBlockedAction.block(waiting, run);
                return CauseOfBlockage.fromMessage(Messages._ModuleQueueDispatcher_blockedByUpstream(run.getFullDisplayName()));
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private RunList<Run<?, ?>> getBuildingRuns(AbstractProject<?, ?> project) {
        return ((RunList<Run<?, ?>>) project.getBuilds()).filter(BUILDING_RUNS_PREDICATE);
    }

    private long waitInterval(ModuleAction module) {
        return Math.max(module.getDependencyWaitInterval(), MINIMUM_WAIT_INTERVAL);
    }

    private long waitedFor(Item waiting) {
        return System.currentTimeMillis() - waiting.getInQueueSince();
    }
}

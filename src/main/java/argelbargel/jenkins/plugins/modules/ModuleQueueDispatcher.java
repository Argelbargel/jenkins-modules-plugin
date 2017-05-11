package argelbargel.jenkins.plugins.modules;


import argelbargel.jenkins.plugins.modules.predicates.QueueDataPredicate;
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
import java.util.Set;


@Extension
public final class ModuleQueueDispatcher extends QueueTaskDispatcher {
    private static final int MINIMUM_WAIT_INTERVAL = 250;
    private static final int MILLIS_PER_SEC = 1000;
    private static final Predicate<Run<?, ?>> BUILDING_RUNS = new Predicate<Run<?, ?>>() {
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
        Module module = project.getTrigger(Module.class);
        return module != null ? canRun(item, project, module) : null;
    }

    private CauseOfBlockage canRun(Item waiting, AbstractProject<?, ?> project, Module module) {
        Set<AbstractProject> upstream = project.getTransitiveUpstreamProjects();
        return !upstream.isEmpty() ? canRun(waiting, upstream, module) : null;
    }

    private CauseOfBlockage canRun(Item waiting, Collection<AbstractProject> upstream, Module module) {
        long timeToWait = waitInterval(module) - waitedFor(waiting);
        if (timeToWait > 0) {
            return CauseOfBlockage.fromMessage(Messages._ModuleQueueDispatcher_waitForDependencies(Math.ceil(timeToWait / (float) MILLIS_PER_SEC)));
        }

        upstream.retainAll(Registry.registry().running());
        return canRun(waiting, upstream, module.getPredicate());
    }

    private CauseOfBlockage canRun(Item waiting, Collection<AbstractProject> running, QueueDataPredicate predicate) {
        for (AbstractProject project : running) {
            Item item = QueueUtils.findBlockingItem(predicate, waiting, Jenkins.getInstance().getQueue().getItems(project));
            if (item != null) {
                return CauseOfBlockage.fromMessage(Messages._ModuleQueueDispatcher_blockedByUpstream(item.task.getDisplayName()));
            }

            Run<?, ?> run = QueueUtils.findBlockingRun(predicate, waiting, getBuildingRuns(project));
            if (run != null) {
                return CauseOfBlockage.fromMessage(Messages._ModuleQueueDispatcher_blockedByUpstream(run.getFullDisplayName()));
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private RunList<Run<?, ?>> getBuildingRuns(AbstractProject<?, ?> project) {
        return ((RunList<Run<?, ?>>) project.getBuilds()).filter(BUILDING_RUNS);
    }

    private int waitInterval(Module module) {
        return Math.max(module.getDependencyWaitInterval() * MILLIS_PER_SEC, MINIMUM_WAIT_INTERVAL);
    }

    private long waitedFor(Item waiting) {
        return System.currentTimeMillis() - waiting.getInQueueSince();
    }
}

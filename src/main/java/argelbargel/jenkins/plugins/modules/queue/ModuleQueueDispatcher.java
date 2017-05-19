package argelbargel.jenkins.plugins.modules.queue;


import argelbargel.jenkins.plugins.modules.Messages;
import argelbargel.jenkins.plugins.modules.ModuleAction;
import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph;
import argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Queue.Item;
import hudson.model.Queue.Task;
import hudson.model.Run;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.logging.Logger;

import static argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate.find;
import static argelbargel.jenkins.plugins.modules.queue.RunUtils.getUncompletedRuns;


@Extension
public final class ModuleQueueDispatcher extends QueueTaskDispatcher {
    private static final int MINIMUM_WAIT_INTERVAL = 250;
    private static final int MILLIS_PER_SEC = 1000;
    private static final Logger LOGGER = Logger.getLogger(ModuleQueueDispatcher.class.getName());

    @Override
    public CauseOfBlockage canRun(Item waiting) {
        return waiting.task instanceof Job ? canRun(waiting, (Job) waiting.task) : super.canRun(waiting);
    }

    private CauseOfBlockage canRun(Item item, Job<?, ?> job) {
        ModuleAction module = ModuleAction.get(job);
        return module != null && ModuleDependencyGraph.get().hasUpstream(job) ? canRun(item, job, module) : null;
    }

    private CauseOfBlockage canRun(Item waiting, Job<?, ?> job, ModuleAction module) {
        long timeToWait = waitInterval(module) - waitedFor(waiting);
        if (timeToWait > 0) {
            LOGGER.info(job.getFullDisplayName() + " must wait " + timeToWait + "ms for dependencies...");
            return CauseOfBlockage.fromMessage(Messages._ModuleQueueDispatcher_waitForDependencies(Math.ceil(timeToWait / (float) MILLIS_PER_SEC)));
        }

        return canRun(waiting, ModuleDependencyGraph.get().getTransitiveUpstream(job), module.getPredicate());
    }

    private CauseOfBlockage canRun(Item waiting, Collection<Job<?, ?>> upstream, ActionsPredicate predicate) {
        for (Job<?, ?> job : upstream) {
            Item item = find(predicate, waiting, Jenkins.getInstance().getQueue().getItems((Task) job));
            if (item != null) {
                LOGGER.info(job.getFullDisplayName() + " is blocked by " + item.task.getFullDisplayName());
                ModuleBlockedAction.block(waiting, item);
                return CauseOfBlockage.fromMessage(Messages._ModuleQueueDispatcher_blockedByUpstream(item.task.getFullDisplayName()));
            }

            Run<?, ?> run = find(predicate, waiting, getUncompletedRuns(job));
            if (run != null) {
                LOGGER.info(job.getFullDisplayName() + " is blocked by " + run.getDisplayName());
                ModuleBlockedAction.block(waiting, run);
                return CauseOfBlockage.fromMessage(Messages._ModuleQueueDispatcher_blockedByUpstream(run.getFullDisplayName()));
            }
        }

        return null;
    }

    private long waitInterval(ModuleAction module) {
        return Math.max(module.getDependencyWaitInterval(), MINIMUM_WAIT_INTERVAL);
    }

    private long waitedFor(Item waiting) {
        return System.currentTimeMillis() - waiting.getInQueueSince();
    }
}

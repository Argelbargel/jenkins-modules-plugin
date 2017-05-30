package argelbargel.jenkins.plugins.modules.queue;


import argelbargel.jenkins.plugins.modules.ModuleAction;
import argelbargel.jenkins.plugins.modules.queue.predicates.QueuePredicate;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.CauseAction;
import hudson.model.Queue.Item;
import hudson.model.Queue.QueueDecisionHandler;
import hudson.model.Queue.Task;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.logging.Logger;

import static argelbargel.jenkins.plugins.modules.queue.predicates.QueuePredicate.filter;


@Extension
public final class ModuleQueueDecisionHandler extends QueueDecisionHandler {
    private static final Logger LOGGER = Logger.getLogger(ModuleQueueDecisionHandler.class.getName());

    @Override
    public boolean shouldSchedule(Task task, List<Action> actions) {
        return !Actionable.class.isInstance(task) || shouldSchedule(task, ModuleAction.get((Actionable) task), actions);
    }

    private boolean shouldSchedule(Task task, ModuleAction module, List<Action> actions) {
        return module == null || shouldSchedule(task, module.getPredicate(), actions);
    }

    private boolean shouldSchedule(Task task, QueuePredicate predicate, List<Action> actions) {
        // only schedule new build when there's no matching build already queued
        Item queued = filter(predicate, actions, Jenkins.getInstance().getQueue().getItems(task));
        if (queued != null) {
            LOGGER.info("will not schedule " + task.getFullDisplayName() + " as " + queued.task.getFullDisplayName() + " is already queued");
            foldActionsInto(queued, actions);
            return false;
        }

        return true;
    }

    private void foldActionsInto(Item queued, List<Action> actions) {
        for (Action a : actions) {
            if (a instanceof CauseAction) {
                ((CauseAction) a).foldIntoExisting(queued, null, actions);
            }
        }
    }
}

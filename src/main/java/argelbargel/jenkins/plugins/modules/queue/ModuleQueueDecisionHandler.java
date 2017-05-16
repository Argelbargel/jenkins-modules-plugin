package argelbargel.jenkins.plugins.modules.queue;


import argelbargel.jenkins.plugins.modules.ModuleAction;
import argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Queue.QueueDecisionHandler;
import hudson.model.Queue.Task;
import jenkins.model.Jenkins;

import java.util.List;

import static argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate.find;


@Extension
public final class ModuleQueueDecisionHandler extends QueueDecisionHandler {
    @Override
    public boolean shouldSchedule(Task task, List<Action> actions) {
        return !Actionable.class.isInstance(task) || shouldSchedule(task, ModuleAction.get((Actionable) task), actions);
    }

    private boolean shouldSchedule(Task task, ModuleAction module, List<Action> actions) {
        return module != null && shouldSchedule(task, module.getPredicate(), actions);
    }

    private boolean shouldSchedule(Task task, ActionsPredicate predicate, List<Action> actions) {
        // only schedule new build when there's no matching build already queued
        return find(predicate, actions, Jenkins.getInstance().getQueue().getItems(task)) == null;
    }
}

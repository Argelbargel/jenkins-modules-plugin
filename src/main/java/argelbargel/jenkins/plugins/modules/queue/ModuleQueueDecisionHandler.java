package argelbargel.jenkins.plugins.modules.queue;


import argelbargel.jenkins.plugins.modules.ModuleAction;
import argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Queue.QueueDecisionHandler;
import hudson.model.Queue.Task;
import jenkins.model.Jenkins;

import java.util.List;

import static argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate.find;


@Extension
public final class ModuleQueueDecisionHandler extends QueueDecisionHandler {
    @Override
    public boolean shouldSchedule(Task p, List<Action> actions) {
        return p instanceof AbstractProject && shouldSchedule((AbstractProject) p, actions);
    }

    private boolean shouldSchedule(AbstractProject project, List<Action> actions) {
        ModuleAction module = ModuleAction.get(project);
        return module != null && shouldSchedule(project, module.getPredicate(), actions);

    }

    private boolean shouldSchedule(AbstractProject project, ActionsPredicate predicate, List<Action> actions) {
        // only schedule new build when there's no matching build already queued
        return find(predicate, actions, Jenkins.getInstance().getQueue().getItems(project)) == null;
    }
}

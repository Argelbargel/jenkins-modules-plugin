package argelbargel.jenkins.plugins.modules.queue;


import argelbargel.jenkins.plugins.modules.ModuleTrigger;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;


@Extension
public final class ModuleRunListener extends RunListener<Run<?, ?>> {
    @Override
    public void onCompleted(Run<?, ?> run, @Nonnull TaskListener listener) {
        onCompleted(run, run.getParent(), listener);
    }

    private void onCompleted(Run<?, ?> run, Job<?, ?> job, TaskListener listener) {
        ModuleTrigger trigger = ModuleTrigger.get(job);
        if (trigger != null) {
            onCompleted(run, trigger, listener);
        }
    }

    private void onCompleted(Run<?, ?> run, ModuleTrigger trigger, TaskListener listener) {
        if (trigger.mustCancelDownstream(run.getResult())) {
            ModuleBlockedAction.cancelItemsBlockedBy(run);
        } else {
            DownstreamTrigger.triggerDownstream(run, listener);
        }
    }
}

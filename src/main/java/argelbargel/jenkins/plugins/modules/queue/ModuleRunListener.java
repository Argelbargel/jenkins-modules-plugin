package argelbargel.jenkins.plugins.modules.queue;


import argelbargel.jenkins.plugins.modules.ModuleAction;
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
        ModuleAction module = ModuleAction.get(job);
        if (module != null) {
            onCompleted(run, module, listener);
        }
    }

    private void onCompleted(Run<?, ?> run, ModuleAction module, TaskListener listener) {
        if (module.mustCancelDownstream(run.getResult())) {
            ModuleBlockedAction.cancelItemsBlockedBy(run);
        } else {
            DownstreamTrigger.triggerDownstream(run, module.getTriggerDownstreamWithCurrentParameters(), listener);
        }

    }
}

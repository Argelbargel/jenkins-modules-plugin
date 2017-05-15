package argelbargel.jenkins.plugins.modules.queue;


import argelbargel.jenkins.plugins.modules.ModuleAction;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.listeners.RunListener;


@Extension
public final class ModuleRunListener extends RunListener<Run<?, ?>> {
    @Override
    public void onFinalized(Run<?, ?> run) {
        onFinalized(run, run.getParent());
    }

    private void onFinalized(Run<?, ?> run, Job<?, ?> job) {
        if (job instanceof AbstractProject) {
            onFinalized(run, (AbstractProject) job);
        }
    }

    private void onFinalized(Run<?, ?> run, AbstractProject<?, ?> project) {
        ModuleAction module = ModuleAction.get(project);
        if (module != null && module.mustCancelDownstream(run.getResult())) {
            ModuleBlockedAction.cancelItemsBlockedBy(run);
        }
    }
}

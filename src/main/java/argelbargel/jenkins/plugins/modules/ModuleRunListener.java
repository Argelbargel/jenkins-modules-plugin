package argelbargel.jenkins.plugins.modules;


import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Result;
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
        Module module = project.getTrigger(Module.class);
        if (module != null && QueueUtils.hasDownstream(project)) {
            stop(run, project, module);
        }
    }

    private void stop(Run<?, ?> run, AbstractProject<?, ?> project, Module module) {
        Registry.registry().stop(module.getName());
        if (mustCancelDownstream(run, module)) {
            QueueUtils.cancelDownstreamForRun(run, project, module.getPredicate());
        }
    }

    private boolean mustCancelDownstream(Run<?, ?> run, Module module) {
        return run.getResult().isWorseThan(Result.fromString(module.getTriggerWhenResultBetterOrEqualTo()));
    }
}

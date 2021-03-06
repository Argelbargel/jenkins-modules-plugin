package argelbargel.jenkins.plugins.modules.views.graph;


import argelbargel.jenkins.plugins.modules.ModuleTrigger;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import jenkins.model.TransientActionFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.singleton;


@Extension
public final class ModuleJobGraphActionFactory extends TransientActionFactory<Job> {
    @Override
    public Class<Job> type() {
        return Job.class;
    }

    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull Job job) {
        ModuleTrigger trigger = ModuleTrigger.get(job);
        return (trigger != null) ? singleton(new ModuleJobGraph(job)) : Collections.<Action>emptyList();
    }
}

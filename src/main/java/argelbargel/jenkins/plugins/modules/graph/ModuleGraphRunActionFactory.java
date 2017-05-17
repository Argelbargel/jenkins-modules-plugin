package argelbargel.jenkins.plugins.modules.graph;


import argelbargel.jenkins.plugins.modules.ModuleTrigger;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.model.TransientActionFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.singleton;


@Extension
public final class ModuleGraphRunActionFactory extends TransientActionFactory<Run> {
    @Override
    public Class<Run> type() {
        return Run.class;
    }

    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull Run run) {
        ModuleTrigger trigger = ParameterizedJobMixIn.getTrigger(run.getParent(), ModuleTrigger.class);
        return (trigger != null) ? singleton(new ModuleGraph(run)) : Collections.<Action>emptyList();
    }
}

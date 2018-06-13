package argelbargel.jenkins.plugins.modules.queue;


import argelbargel.jenkins.plugins.modules.Messages;
import argelbargel.jenkins.plugins.modules.ModuleDependency;
import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph;
import argelbargel.jenkins.plugins.modules.ModuleTrigger;
import argelbargel.jenkins.plugins.modules.parameters.TriggerParameter;
import hudson.console.ModelHyperlinkNote;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.FileParameterValue;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.security.ACLContext;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static argelbargel.jenkins.plugins.modules.queue.RunUtils.getUncompletedRuns;


/**
 * adapted and refactored version of Jenkins BuildTrigger for AbstractProject
 *
 * @see AbstractProject
 * @see hudson.tasks.BuildTrigger
 */
class DownstreamTrigger {
    private final Run<?, ?> upstreamBuild;
    private final ModuleTrigger moduleTrigger;
    private final PrintStream logger;

    DownstreamTrigger(Run<?, ?> upstream, ModuleTrigger trigger, TaskListener listener) {
        upstreamBuild = upstream;
        moduleTrigger = trigger;
        logger = listener.getLogger();
    }

    void execute() {
        if (moduleTrigger.shouldTriggerDownstream(upstreamBuild)) {
            execute(ModuleDependencyGraph.get().getDownstreamDependencies(upstreamBuild.getParent()));
        }
    }

    private void execute(final List<ModuleDependency> downstream) {
        try (ACLContext ctx = ACL.as(Jenkins.getAuthentication())) {
            for (ModuleDependency dep : downstream) {
                if (isNotAlreadyRunning(dep) && dep.shouldTriggerBuild()) {
                    Job job = dep.getDownstreamJob();
                    if (!job.hasPermission(Item.BUILD)) {
                        logger.println(hudson.tasks.Messages.BuildTrigger_you_have_no_permission_to_build_(ModelHyperlinkNote.encodeTo(job)));
                        continue;
                    }

                    Collection<Action> actions = createDownstreamActions(job);
                    triggerDownstreamBuild(job, actions.toArray(new Action[0]));
                }
            }
        }
    }

    private boolean isNotAlreadyRunning(ModuleDependency dep) {
        return isNotAlreadyRunning(dep.getDownstreamJob());
    }

    private boolean isNotAlreadyRunning(Job<?, ?> job) {
        for (Run<?, ?> run : getUncompletedRuns(job)) {
            ModuleBlockedAction blocked = ModuleBlockedAction.get(run);
            if (blocked != null && blocked.wasBlockedBy(upstreamBuild)) {
                logger.println(Messages.DownstreamTrigger_AlreadyRunning(run.getDisplayName()));
                return false;
            }
        }

        return true;
    }

    private Collection<Action> createDownstreamActions(Job<?, ?> downstream) {
        Collection<Action> actions = new ArrayList<>(2);
        actions.add(new CauseAction(new Cause.UpstreamCause(upstreamBuild)));

        ParametersDefinitionProperty property = downstream.getProperty(ParametersDefinitionProperty.class);
        if (property != null) {
            List<ParameterValue> values = new ArrayList<>();
            for (ParameterValue value : createDownstreamParameters()) {
                if (property.getParameterDefinitionNames().contains(value.getName())) {
                    values.add(value);
                }
            }
            if (!values.isEmpty()) {
                actions.add(new ParametersAction(values));
            }
        }

        return actions;
    }

    private Collection<ParameterValue> createDownstreamParameters() {
        Map<String, ParameterValue> values = new HashMap<>();
        if (moduleTrigger.getTriggerWithCurrentParameters()) {
            ParametersAction action = upstreamBuild.getAction(ParametersAction.class);
            if (action != null) {
                for (ParameterValue value : action.getParameters())
                    // FileParameterValue is currently not reusable, so omit these:
                    if (!(value instanceof FileParameterValue)) {
                        values.put(value.getDescription(), value);
                    }
            }
        }

        for (TriggerParameter parameter : moduleTrigger.getDownstreamParameters()) {
            ParameterValue value = parameter.createParameterValue();
            values.put(value.getName(), value);
        }

        return values.values();
    }

    private void triggerDownstreamBuild(Job<?, ?> job, Action[] actions) {
        if (isEnabled(job, logger)) {
            boolean scheduled = scheduleBuild(job, actions);
            if (Jenkins.get().getItemByFullName(job.getFullName()) == job) {
                String name = ModelHyperlinkNote.encodeTo(job);
                if (scheduled) {
                    logger.println(hudson.tasks.Messages.BuildTrigger_Triggering(name));
                } else {
                    logger.println(hudson.tasks.Messages.BuildTrigger_InQueue(name));
                }
            } // otherwise upstream users should not know that it happened
        }
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean scheduleBuild(Job<?, ?> job, Action[] actions) {
        return ParameterizedJobMixIn.scheduleBuild2(job, ((ParameterizedJob) job).getQuietPeriod(), actions) != null;
    }

    private boolean isEnabled(Job<?, ?> job, PrintStream logger) {
        if (job instanceof AbstractProject && ((AbstractProject) job).isDisabled()) {
            logger.println(hudson.tasks.Messages.BuildTrigger_Disabled(ModelHyperlinkNote.encodeTo(job)));
            return false;
        }

        return true;
    }
}

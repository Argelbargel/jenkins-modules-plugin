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
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import jenkins.security.QueueItemAuthenticatorConfiguration;
import jenkins.security.QueueItemAuthenticatorDescriptor;
import org.acegisecurity.Authentication;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        ACL.impersonate(checkAuth(downstream), new Runnable() {
            @Override
            public void run() {
                for (ModuleDependency dep : downstream) {
                    if (isNotAlreadyRunning(dep) && dep.shouldTriggerBuild()) {
                        Collection<Action> actions = createDownstreamActions(dep.getDownstreamJob());
                        triggerDownstreamBuild(dep.getDownstreamJob(), actions.toArray(new Action[actions.size()]));
                    }
                }
            }
        });
    }

    private Authentication checkAuth(List<ModuleDependency> downstream) {
        Authentication auth = Jenkins.getAuthentication(); // from build
        if (auth.equals(ACL.SYSTEM)) { // i.e., unspecified
            if (QueueItemAuthenticatorDescriptor.all().isEmpty()) {
                if (!downstream.isEmpty()) {
                    logger.println(hudson.tasks.Messages.BuildTrigger_warning_you_have_no_plugins_providing_ac());
                }
            } else if (QueueItemAuthenticatorConfiguration.get().getAuthenticators().isEmpty()) {
                if (!downstream.isEmpty()) {
                    logger.println(hudson.tasks.Messages.BuildTrigger_warning_access_control_for_builds_in_glo());
                }
            } else {
                // This warning must be printed even if downstream is empty.
                // Otherwise you could effectively escalate DISCOVER to READ just by trying different project names and checking whether a warning was printed or not.
                // If there were an API to determine whether any DependencyDeclarer’s in this project requested downstream project names,
                // then we could suppress the warnings in case none did; but if any do, yet Items.fromNameList etc. ignore unknown projects,
                // that has to be treated the same as if there really are downstream projects but the anonymous user cannot see them.
                // For the above two cases, it is OK to suppress the warning when there are no downstream projects, since running as SYSTEM we would be able to see them anyway.
                logger.println(hudson.tasks.Messages.BuildTrigger_warning_this_build_has_no_associated_aut());
                return Jenkins.ANONYMOUS;
            }
        }
        return auth;
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

    private List<ParameterValue> createDownstreamParameters() {
        List<ParameterValue> values = new ArrayList<>();
        if (moduleTrigger.getTriggerWithCurrentParameters()) {
            ParametersAction action = upstreamBuild.getAction(ParametersAction.class);
            if (action != null) {
                for (ParameterValue value : action.getParameters())
                    // FileParameterValue is currently not reusable, so omit these:
                    if (!(value instanceof FileParameterValue)) {
                        values.add(value);
                    }
            }
        }

        for (TriggerParameter parameter : moduleTrigger.getDownstreamParameters()) {
            values.add(parameter.createParameterValue());
        }

        return values;
    }

    private void triggerDownstreamBuild(Job<?, ?> job, Action[] actions) {
        if (isEnabled(job, logger)) {
            boolean scheduled = scheduleBuild(job, actions);
            if (Jenkins.getInstance().getItemByFullName(job.getFullName()) == job) {
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

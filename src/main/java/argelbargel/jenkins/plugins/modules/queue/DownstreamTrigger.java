package argelbargel.jenkins.plugins.modules.queue;


import argelbargel.jenkins.plugins.modules.Messages;
import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph;
import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph.Dependency;
import hudson.console.ModelHyperlinkNote;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.FileParameterValue;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import jenkins.security.QueueItemAuthenticatorConfiguration;
import jenkins.security.QueueItemAuthenticatorDescriptor;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;

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
    static void triggerDownstream(Run<?, ?> reason, boolean withCurrentParameters, TaskListener listener) {
        execute(reason, reason.getParent(), withCurrentParameters, listener);
    }

    private static void execute(Run<?, ?> reason, Job<?, ?> job, boolean withCurrentParameters, TaskListener listener) {
        List<Dependency> downstream = ModuleDependencyGraph.get().getDownstreamDependencies(job);
        Authentication auth = checkAuth(downstream, listener.getLogger());
        execute(reason, withCurrentParameters, downstream, auth, listener);
    }

    private static void execute(Run<?, ?> reason, boolean withCurrentParameters, List<Dependency> downstream, Authentication auth, TaskListener listener) {
        for (Dependency dep : downstream) {
            List<Action> buildActions = new ArrayList<>();
            SecurityContext orig = ACL.impersonate(auth);
            try {
                if (isNotAlreadyRunning(dep, reason, listener) && dep.shouldTriggerBuild(reason, listener, buildActions)) {
                    buildActions.addAll(createBuildActions(reason, withCurrentParameters));
                    execute(dep.getDownstreamJob(), buildActions.toArray(new Action[buildActions.size()]), listener.getLogger());
                }
            } finally {
                SecurityContextHolder.setContext(orig);
            }
        }

    }

    private static boolean isNotAlreadyRunning(Dependency dep, Run<?, ?> reason, TaskListener listener) {
        return isNotAlreadyRunning(dep.getDownstreamJob(), reason, listener.getLogger());
    }

    private static boolean isNotAlreadyRunning(Job<?, ?> job, Run<?, ?> reason, PrintStream logger) {
        for (Run<?, ?> run : getUncompletedRuns(job)) {
            ModuleBlockedAction blocked = ModuleBlockedAction.get(run);
            if (blocked != null && blocked.wasBlockedBy(reason)) {
                logger.println(Messages.DownstreamTrigger_AlreadyRunning(run.getDisplayName()));
                return false;
            }
        }

        return true;
    }

    private static Collection<? extends Action> createBuildActions(Run<?, ?> run, boolean withCurrentParameters) {
        Collection<Action> actions = new ArrayList<>();
        actions.add(new CauseAction(new Cause.UpstreamCause(run)));

        if (withCurrentParameters) {
            ParametersAction action = run.getAction(ParametersAction.class);
            if (action != null) {
                List<ParameterValue> values = new ArrayList<>(action.getParameters().size());
                for (ParameterValue value : action.getParameters())
                    // FileParameterValue is currently not reusable, so omit these:
                    if (!(value instanceof FileParameterValue)) {
                        values.add(value);
                    }
                actions.add(new ParametersAction(values));
            }
        }

        return actions;
    }

    private static void execute(Job<?, ?> job, Action[] actions, PrintStream logger) {
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
    private static boolean scheduleBuild(Job<?, ?> job, Action[] actions) {
        return ParameterizedJobMixIn.scheduleBuild2(job, ((ParameterizedJob) job).getQuietPeriod(), actions) != null;
    }

    private static boolean isEnabled(Job<?, ?> job, PrintStream logger) {
        if (job instanceof AbstractProject && ((AbstractProject) job).isDisabled()) {
            logger.println(hudson.tasks.Messages.BuildTrigger_Disabled(ModelHyperlinkNote.encodeTo(job)));
            return false;
        }

        return true;
    }

    private static Authentication checkAuth(List<Dependency> downstream, PrintStream logger) {
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

    private DownstreamTrigger() { /* no instances allowed */ }
}

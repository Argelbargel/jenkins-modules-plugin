package argelbargel.jenkins.plugins.modules.queue.predicates;


import hudson.ExtensionPoint;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Descriptor;
import hudson.model.Items;
import hudson.model.Run;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static argelbargel.jenkins.plugins.modules.queue.predicates.Actions.create;


public abstract class QueuePredicate extends AbstractDescribableImpl<QueuePredicate> implements ExtensionPoint, Serializable {
    public static <SUBJECT extends Actionable> SUBJECT filter(QueuePredicate predicate, Actionable reason, Iterable<SUBJECT> items) {
        return filter(predicate, create(reason), items);
    }

    public static <SUBJECT extends Actionable> SUBJECT filter(QueuePredicate predicate, List<Action> reason, Iterable<SUBJECT> items) {
        return filter(predicate, create(reason), items);
    }

    private static <SUBJECT extends Actionable> SUBJECT filter(QueuePredicate predicate, Actions reason, Iterable<SUBJECT> items) {
        for (SUBJECT subject : items) {
            if (predicate.test(reason, create(subject))) {
                return subject;
            }
        }

        return null;
    }

    protected abstract boolean test(@Nonnull Actions reason, @Nonnull Actions subject);


    public static abstract class ActionsPredicateDescriptor extends Descriptor<QueuePredicate> {
        public static List<ActionsPredicateDescriptor> getAll() {
            return Jenkins.getInstance().getDescriptorList(QueuePredicate.class);
        }
    }


    private static final Map<String, Class> COMPATIBILITY_ALIASES = new HashMap<>();

    static {
        COMPATIBILITY_ALIASES.put("argelbargel.jenkins.plugins.modules.predicates.AndActionsPredicate", AndQueuePredicate.class);
        COMPATIBILITY_ALIASES.put("argelbargel.jenkins.plugins.modules.predicates.OrActionsPredicate", OrQueuePredicate.class);
        COMPATIBILITY_ALIASES.put("argelbargel.jenkins.plugins.modules.predicates.SameBuildParameters", StartedWithSameBuildParametersQueuePredicate.class);
        COMPATIBILITY_ALIASES.put("argelbargel.jenkins.plugins.modules.predicates.StartedBySameGitlabUserPredicate", StartedBySameGitlabUserQueuePredicate.class);
        COMPATIBILITY_ALIASES.put("argelbargel.jenkins.plugins.modules.predicates.StartedBySameUserPredicate", StartedBySameUserQueuePredicate.class);
        COMPATIBILITY_ALIASES.put("argelbargel.jenkins.plugins.modules.predicates.StartedByUserPredicate", StartedBySameUserQueuePredicate.class);
        COMPATIBILITY_ALIASES.put("argelbargel.jenkins.plugins.modules.predicates.UpstreamBuildPredicate", UpstreamBuildQueuePredicate.class);
    }

    @Deprecated // >= 0.8
    @Initializer(before = InitMilestone.PLUGINS_STARTED)
    public static void addCompatibilityAliases() {
        for (Map.Entry<String, Class> entry : COMPATIBILITY_ALIASES.entrySet()) {
            Items.XSTREAM2.addCompatibilityAlias(entry.getKey(), entry.getValue());
            Run.XSTREAM2.addCompatibilityAlias(entry.getKey(), entry.getValue());
        }
    }
}

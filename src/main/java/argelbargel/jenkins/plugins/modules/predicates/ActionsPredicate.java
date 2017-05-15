package argelbargel.jenkins.plugins.modules.predicates;


import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

import static argelbargel.jenkins.plugins.modules.predicates.Actions.create;


public abstract class ActionsPredicate extends AbstractDescribableImpl<ActionsPredicate> implements ExtensionPoint, Serializable {
    @SuppressWarnings({"unused"}) // part of public API
    public static final ActionsPredicate ANY = new ActionsPredicate() {
        @Override
        public boolean test(@Nonnull Actions reason, @Nonnull Actions subject) {
            return true;
        }
    };

    @SuppressWarnings("WeakerAccess") // part of public API
    public static final ActionsPredicate NONE = new ActionsPredicate() {
        @Override
        public boolean test(@Nonnull Actions reason, @Nonnull Actions subject) {
            return false;
        }
    };

    public static <SUBJECT extends Actionable> SUBJECT find(ActionsPredicate predicate, Actionable reason, Iterable<SUBJECT> items) {
        return find(predicate, create(reason), items);
    }

    public static <SUBJECT extends Actionable> SUBJECT find(ActionsPredicate predicate, List<Action> reason, Iterable<SUBJECT> items) {
        return find(predicate, create(reason), items);
    }

    private static <SUBJECT extends Actionable> SUBJECT find(ActionsPredicate predicate, Actions reason, Iterable<SUBJECT> items) {
        for (SUBJECT subject : items) {
            if (predicate.test(reason, create(subject))) {
                return subject;
            }
        }

        return null;
    }

    public final boolean test(@Nonnull Actionable reason, @Nonnull Actionable subject) {
        return test(create(reason), create(subject));
    }

    public final boolean test(@Nonnull List<Action> reason, @Nonnull Actionable subject) {
        return test(create(reason), create(subject));
    }

    public final boolean test(@Nonnull List<Action> reason, @Nonnull List<Action> subject) {
        return test(create(reason), create(subject));
    }

    public final boolean test(@Nonnull Actionable reason, @Nonnull List<Action> subject) {
        return test(create(reason), create(subject));
    }

    protected abstract boolean test(@Nonnull Actions reason, @Nonnull Actions subject);


    public static abstract class ActionsPredicateDescriptor extends Descriptor<ActionsPredicate> {
        public static List<ActionsPredicateDescriptor> getAll() {
            return Jenkins.getInstance().getDescriptorList(ActionsPredicate.class);
        }
    }
}

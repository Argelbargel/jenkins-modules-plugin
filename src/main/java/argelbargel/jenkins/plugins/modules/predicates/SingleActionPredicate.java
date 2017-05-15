package argelbargel.jenkins.plugins.modules.predicates;


import hudson.model.Action;

import javax.annotation.Nonnull;


public abstract class SingleActionPredicate<ACTION extends Action> extends ActionsPredicate {
    private final Class<ACTION> actionType;

    @SuppressWarnings("WeakerAccess") // part of public API
    protected SingleActionPredicate(Class<ACTION> type) {
        this.actionType = type;
    }

    @Override
    public final boolean test(@Nonnull Actions reason, @Nonnull Actions subject) {
        ACTION reasonAction = reason.getAction(actionType);
        ACTION subjectAction = subject.getAction(actionType);

        return !(reasonAction == null || subjectAction == null) && test(reasonAction, subjectAction);
    }

    protected abstract boolean test(ACTION reason, ACTION subject);
}

package argelbargel.jenkins.plugins.modules.queue.predicates;


import hudson.model.Action;

import javax.annotation.Nonnull;


public abstract class SingleActionQueuePredicate<ACTION extends Action> extends QueuePredicate {
    private final Class<ACTION> actionType;

    @SuppressWarnings("WeakerAccess") // part of public API
    protected SingleActionQueuePredicate(Class<ACTION> type) {
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

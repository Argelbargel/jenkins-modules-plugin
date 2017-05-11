package argelbargel.jenkins.plugins.modules.predicates;


import argelbargel.jenkins.plugins.modules.QueueData;
import hudson.model.Cause;

import javax.annotation.Nonnull;


public abstract class CausePredicate<CAUSE extends Cause> extends QueueDataPredicate {
    private final Class<CAUSE> type;

    protected CausePredicate(Class<CAUSE> type) {
        this.type = type;
    }

    @Override
    public final boolean test(@Nonnull QueueData reason, @Nonnull QueueData subject) {
        CAUSE reasonCause = reason.getCause(type);
        CAUSE subjectCause = subject.getCause(type);

        return !(reasonCause == null || subjectCause == null) && test(reasonCause, subjectCause);
    }

    protected abstract boolean test(CAUSE reasonCause, CAUSE subjectCause);
}

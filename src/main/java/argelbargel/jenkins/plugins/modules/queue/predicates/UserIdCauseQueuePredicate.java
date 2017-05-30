package argelbargel.jenkins.plugins.modules.queue.predicates;


import hudson.model.Cause.UserIdCause;

import javax.annotation.Nonnull;


public abstract class UserIdCauseQueuePredicate extends CauseActionQueuePredicate<UserIdCause> {
    @SuppressWarnings("WeakerAccess") // part of public API
    protected UserIdCauseQueuePredicate(boolean upstream) {
        super(UserIdCause.class, upstream);
    }

    protected abstract boolean test(@Nonnull UserIdCause reason, @Nonnull UserIdCause subject);
}

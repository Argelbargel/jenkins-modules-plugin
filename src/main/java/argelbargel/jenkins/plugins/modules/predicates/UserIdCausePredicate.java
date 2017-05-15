package argelbargel.jenkins.plugins.modules.predicates;


import hudson.model.Cause.UserIdCause;

import javax.annotation.Nonnull;


public abstract class UserIdCausePredicate extends CauseActionPredicate<UserIdCause> {
    @SuppressWarnings("WeakerAccess") // part of public API
    protected UserIdCausePredicate(boolean upstream) {
        super(UserIdCause.class, upstream);
    }

    protected abstract boolean test(@Nonnull UserIdCause reason, @Nonnull UserIdCause subject);
}

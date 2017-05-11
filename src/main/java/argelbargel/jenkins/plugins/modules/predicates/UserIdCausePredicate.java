package argelbargel.jenkins.plugins.modules.predicates;


import hudson.model.Cause.UserIdCause;

import javax.annotation.Nonnull;


public abstract class UserIdCausePredicate extends SingleCausePredicate<UserIdCause> {
    protected UserIdCausePredicate() {
        super(UserIdCause.class);
    }

    protected abstract boolean test(@Nonnull UserIdCause reason, @Nonnull UserIdCause subject);
}

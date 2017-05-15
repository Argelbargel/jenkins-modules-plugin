package argelbargel.jenkins.plugins.modules.predicates;


import hudson.Extension;
import hudson.model.Cause.UserIdCause;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


public class StartedBySameUserPredicate extends UserIdCausePredicate {
    @DataBoundConstructor
    public StartedBySameUserPredicate(boolean checkUpstream) {
        super(checkUpstream);
    }

    @Override
    protected boolean test(@Nonnull UserIdCause reason, @Nonnull UserIdCause subject) {
        return reason.equals(subject);
    }


    @Extension
    @Symbol("startedBySameUserPredicate")
    public static final class DescriptorImpl extends CauseActionPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Started by the same user";
        }
    }
}

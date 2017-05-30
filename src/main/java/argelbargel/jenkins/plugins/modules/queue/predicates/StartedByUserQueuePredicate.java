package argelbargel.jenkins.plugins.modules.queue.predicates;


import hudson.Extension;
import hudson.model.Cause.UserIdCause;
import hudson.model.User;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


@SuppressWarnings("unused") // extension
public class StartedByUserQueuePredicate extends UserIdCauseQueuePredicate {
    private final String userId;

    @DataBoundConstructor
    public StartedByUserQueuePredicate(String userId, boolean checkUpstream) {
        super(checkUpstream);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    protected boolean test(@Nonnull UserIdCause reason, @Nonnull UserIdCause subject) {
        return userId.equals(subject.getUserId());
    }


    @Extension
    @Symbol("startedByUserPredicate")
    public static final class DescriptorImpl extends CauseActionPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Started by a specific user";
        }

        @Restricted(NoExternalUse.class)
        public ListBoxModel doFillUserIdItems() {
            final ListBoxModel model = new ListBoxModel();
            ACL.impersonate(ACL.SYSTEM, new Runnable() {
                @Override
                public void run() {
                    for (User user : User.getAll()) {
                        model.add(user.getDisplayName(), user.getId());
                    }
                }
            });
            return model;
        }
    }
}

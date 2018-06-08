package argelbargel.jenkins.plugins.modules.queue.predicates;


import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;


public final class AndQueuePredicate extends CombinedQueuePredicate {
    @DataBoundConstructor
    public AndQueuePredicate(@Nonnull List<QueuePredicate> predicates) {
        super(predicates);
    }

    @Override
    boolean test(List<QueuePredicate> predicates, Actions reason, Actions subject) {
        for (QueuePredicate predicate : predicates) {
            if (!predicate.test(reason, subject)) {
                return false;
            }
        }

        return true;
    }


    @Extension
    @Symbol("andQueuePredicate")
    public static final class DescriptorImpl extends QueuePredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "AND";
        }
    }
}

package argelbargel.jenkins.plugins.modules.queue.predicates;


import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;


@SuppressWarnings("unused") // extension
public final class OrQueuePredicate extends CombinedQueuePredicate {
    @DataBoundConstructor
    public OrQueuePredicate(@Nonnull List<QueuePredicate> predicates) {
        super(predicates);
    }

    @Override
    boolean test(List<QueuePredicate> predicates, Actions reason, Actions subject) {
        for (QueuePredicate predicate : predicates) {
            if (predicate.test(reason, subject)) {
                return true;
            }
        }

        return false;
    }


    @Extension
    @Symbol("orQueuePredicate")
    public static final class DescriptorImpl extends ActionsPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "OR";
        }
    }
}

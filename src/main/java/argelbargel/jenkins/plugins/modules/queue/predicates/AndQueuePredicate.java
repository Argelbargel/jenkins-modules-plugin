package argelbargel.jenkins.plugins.modules.queue.predicates;


import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;


public final class AndQueuePredicate extends CombinedQueuePredicate {
    @DataBoundConstructor
    public AndQueuePredicate(@Nonnull List<QueuePredicate> predicates) {
        super(predicates);
    }

    @Override
    boolean test(Stream<QueuePredicate> predicates, Actions reason, Actions subject) {
        return predicates.allMatch(p -> p.test(reason, subject));
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

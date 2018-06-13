package argelbargel.jenkins.plugins.modules.queue.predicates;


import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;


@SuppressWarnings("unused") // extension
public final class OrQueuePredicate extends CombinedQueuePredicate {
    @DataBoundConstructor
    public OrQueuePredicate(@Nonnull List<QueuePredicate> predicates) {
        super(predicates);
    }

    @Override
    boolean test(Stream<QueuePredicate> predicates, Actions reason, Actions subject) {
        return predicates.anyMatch(p -> p.test(reason, subject));
    }


    @Extension
    @Symbol("orQueuePredicate")
    public static final class DescriptorImpl extends QueuePredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "OR";
        }
    }
}

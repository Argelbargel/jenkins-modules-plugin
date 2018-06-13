package argelbargel.jenkins.plugins.modules.queue.predicates;


import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;


public abstract class CombinedQueuePredicate extends QueuePredicate {
    private List<QueuePredicate> predicates;

    CombinedQueuePredicate(@Nonnull List<QueuePredicate> predicates) {
        this.predicates = predicates;
    }

    public List<QueuePredicate> getPredicates() {
        return predicates;
    }

    @DataBoundSetter
    public void setPredicates(@Nonnull List<QueuePredicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public final boolean test(@Nonnull Actions reason, @Nonnull Actions subject) {
        return test(predicates.stream(), reason, subject);
    }

    abstract boolean test(Stream<QueuePredicate> predicates, Actions reason, Actions subject);
}

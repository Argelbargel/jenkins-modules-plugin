package argelbargel.jenkins.plugins.modules.predicates;


import argelbargel.jenkins.plugins.modules.QueueData;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.List;


abstract class CombinedQueueDataPredicate extends QueueDataPredicate {
    private List<QueueDataPredicate> predicates;

    CombinedQueueDataPredicate(@Nonnull List<QueueDataPredicate> predicates) {
        this.predicates = predicates;
    }

    @Nonnull
    public final List<QueueDataPredicate> getPredicates() {
        return predicates;
    }

    @DataBoundSetter
    public final void setPredicates(@Nonnull List<QueueDataPredicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public final boolean test(@Nonnull QueueData reason, @Nonnull QueueData subject) {
        return test(predicates, reason, subject);
    }

    abstract boolean test(List<QueueDataPredicate> predicates, QueueData reason, QueueData subject);
}

package argelbargel.jenkins.plugins.modules.predicates;


import argelbargel.jenkins.plugins.modules.QueueData;
import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;


public final class OrQueueDataPredicate extends CombinedQueueDataPredicate {
    @DataBoundConstructor
    public OrQueueDataPredicate() {
        this(Collections.<QueueDataPredicate>emptyList());
    }

    public OrQueueDataPredicate(@Nonnull List<QueueDataPredicate> predicates) {
        super(predicates);
    }

    @Override
    boolean test(List<QueueDataPredicate> predicates, QueueData reason, QueueData subject) {
        for (QueueDataPredicate predicate : predicates) {
            if (predicate.test(reason, subject)) {
                return true;
            }
        }

        return false;
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

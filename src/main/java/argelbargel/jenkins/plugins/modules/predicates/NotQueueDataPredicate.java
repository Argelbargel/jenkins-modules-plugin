package argelbargel.jenkins.plugins.modules.predicates;


import argelbargel.jenkins.plugins.modules.QueueData;
import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;


public final class NotQueueDataPredicate extends QueueDataPredicate {
    private QueueDataPredicate predicate;

    @DataBoundConstructor
    public NotQueueDataPredicate() {
        this(NONE);
    }

    public NotQueueDataPredicate(@Nonnull QueueDataPredicate predicate) {
        this.predicate = predicate;
    }

    @Nonnull
    public QueueDataPredicate getPredicate() {
        return predicate;
    }

    @DataBoundSetter
    public void setPredicate(@Nonnull QueueDataPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(@Nonnull QueueData reason, @Nonnull QueueData subject) {
        return !predicate.test(reason, subject);
    }


    @Extension
    @Symbol("notQueuePredicate")
    public static final class DescriptorImpl extends QueuePredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "NOT";
        }
    }

}

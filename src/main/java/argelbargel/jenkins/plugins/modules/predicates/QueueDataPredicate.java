package argelbargel.jenkins.plugins.modules.predicates;


import argelbargel.jenkins.plugins.modules.QueueData;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;


public abstract class QueueDataPredicate extends AbstractDescribableImpl<QueueDataPredicate> implements ExtensionPoint, Serializable {
    protected static final QueueDataPredicate ANY = new QueueDataPredicate() {
        @Override
        public boolean test(@Nonnull QueueData reason, @Nonnull QueueData subject) {
            return true;
        }
    };

    protected static final QueueDataPredicate NONE = new QueueDataPredicate() {
        @Override
        public boolean test(@Nonnull QueueData reason, @Nonnull QueueData subject) {
            return false;
        }
    };

    public abstract boolean test(@Nonnull QueueData reason, @Nonnull QueueData subject);


    public static abstract class QueuePredicateDescriptor extends Descriptor<QueueDataPredicate> {
        public static List<QueuePredicateDescriptor> getAll() {
            return Jenkins.getInstance().getDescriptorList(QueueDataPredicate.class);
        }
    }
}

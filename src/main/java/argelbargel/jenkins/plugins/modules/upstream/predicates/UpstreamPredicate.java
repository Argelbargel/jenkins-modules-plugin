package argelbargel.jenkins.plugins.modules.upstream.predicates;


import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Job;
import jenkins.model.Jenkins;

import java.io.Serializable;
import java.util.List;


public abstract class UpstreamPredicate extends AbstractDescribableImpl<UpstreamPredicate> implements ExtensionPoint, Serializable {
    public abstract boolean test(Job<?, ?> upstream, Job<?, ?> downstream);

    public UpstreamPredicate reset() {
        return this;
    }

    public int compare(Job<?, ?> upstream1, Job<?, ?> upstream2, Job<?, ?> downstream) {
        boolean test1 = test(upstream1, downstream);
        boolean test2 = test(upstream2, downstream);

        if (test1 == test2) {
            return 0;
        }

        return (test1) ? -1 : 1;
    }

    public static abstract class UpstreamPredicateDescriptor extends Descriptor<UpstreamPredicate> {
        public static List<UpstreamPredicateDescriptor> getAllUpstreamPredicateDescriptors() {
            return Jenkins.get().getDescriptorList(UpstreamPredicate.class);
        }
    }
}

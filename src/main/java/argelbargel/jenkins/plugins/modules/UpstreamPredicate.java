package argelbargel.jenkins.plugins.modules;


import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Job;
import jenkins.model.Jenkins;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;


public abstract class UpstreamPredicate extends AbstractDescribableImpl<UpstreamPredicate> implements ExtensionPoint, Predicate<Job<?, ?>>, Serializable {
    public static abstract class UpstreamPredicateDescriptor extends Descriptor<UpstreamPredicate> {
        public static List<UpstreamPredicateDescriptor> getAllUpstreamPredicateDescriptors() {
            return Jenkins.get().getDescriptorList(UpstreamPredicate.class);
        }
    }
}

package argelbargel.jenkins.plugins.modules.upstream.predicates;


import hudson.Extension;
import hudson.model.Job;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


@SuppressWarnings("unused") // extension
public class LimitUpstreamPredicate extends UpstreamPredicate {
    private int limit;
    private transient int matches;

    @DataBoundConstructor
    public LimitUpstreamPredicate(int limit) {
        this.limit = limit;
        this.matches = 0;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public boolean test(Job<?, ?> upstream, Job<?, ?> downstream) {
        return limit > matches++;
    }

    @Override
    public UpstreamPredicate reset() {
        matches = 0;
        return super.reset();
    }


    @Extension
    @Symbol("limitUpstream")
    public static final class DescriptorImpl extends UpstreamPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Limit upstream";
        }
    }
}

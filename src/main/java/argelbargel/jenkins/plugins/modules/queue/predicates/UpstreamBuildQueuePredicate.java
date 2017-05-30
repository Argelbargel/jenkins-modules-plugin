package argelbargel.jenkins.plugins.modules.queue.predicates;


import hudson.Extension;
import hudson.model.Cause.UpstreamCause;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


@SuppressWarnings("unused") // extension
public final class UpstreamBuildQueuePredicate extends CauseActionQueuePredicate<UpstreamCause> {
    @DataBoundConstructor
    public UpstreamBuildQueuePredicate() {
        super(UpstreamCause.class, true);
    }

    @Override
    protected boolean test(UpstreamCause reasonCause, UpstreamCause subjectCause) {
        return reasonCause.equals(subjectCause);
    }

    @Extension
    @Symbol("startedBySameUpstreamBuild")
    public static final class DescriptorImpl extends CauseActionPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Started by the same upstream-build";
        }
    }
}

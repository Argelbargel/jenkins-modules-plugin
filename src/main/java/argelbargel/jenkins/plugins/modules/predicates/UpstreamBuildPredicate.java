package argelbargel.jenkins.plugins.modules.predicates;


import hudson.Extension;
import hudson.model.Cause.UpstreamCause;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


@SuppressWarnings("unused") // extension
public final class UpstreamBuildPredicate extends CauseActionPredicate<UpstreamCause> {
    @DataBoundConstructor
    public UpstreamBuildPredicate() {
        super(UpstreamCause.class, true);
    }

    @Override
    protected boolean test(UpstreamCause reasonCause, UpstreamCause subjectCause) {
        return reasonCause.getUpstreamProject().equals(subjectCause.getUpstreamProject())
                && reasonCause.getUpstreamBuild() == subjectCause.getUpstreamBuild();
    }

    @Extension
    @Symbol("upstreamBuildPredicate")
    public static final class DescriptorImpl extends CauseActionPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Started by the same upstream-build";
        }
    }
}

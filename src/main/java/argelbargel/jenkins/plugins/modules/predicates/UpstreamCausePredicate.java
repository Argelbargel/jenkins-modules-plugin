package argelbargel.jenkins.plugins.modules.predicates;


import hudson.model.Cause.UpstreamCause;
import org.kohsuke.stapler.DataBoundConstructor;


public class UpstreamCausePredicate extends SingleCausePredicate<UpstreamCause> {
    @DataBoundConstructor
    public UpstreamCausePredicate() {
        super(UpstreamCause.class);
    }

    @Override
    protected boolean test(UpstreamCause reason, UpstreamCause subject) {
        return false;
    }
}

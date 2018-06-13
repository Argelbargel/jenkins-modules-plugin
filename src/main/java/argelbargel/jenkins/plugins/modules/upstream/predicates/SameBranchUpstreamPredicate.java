package argelbargel.jenkins.plugins.modules.upstream.predicates;


import hudson.model.Job;
import jenkins.branch.Branch;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.variant.OptionalExtension;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

import static java.util.Optional.ofNullable;


@SuppressWarnings("unused") // extension
public class SameBranchUpstreamPredicate extends UpstreamPredicate {
    @DataBoundConstructor
    public SameBranchUpstreamPredicate() { /* NOOP */ }


    @Override
    public final boolean test(Job<?, ?> upstream, Job<?, ?> downstream) {
        return test(getBranch(upstream), getBranch(downstream));
    }

    private boolean test(Branch upstream, Branch downstream) {
        if (downstream == null || upstream == null) {
            return false;
        }

        return downstream.getName().equals(upstream.getName());
    }

    private Branch getBranch(Job<?, ?> job) {
        return ofNullable(job.getProperty(BranchJobProperty.class))
                .map(BranchJobProperty::getBranch)
                .orElse(null);
    }


    @OptionalExtension(requirePlugins = {"branch-api", "workflow-multibranch"})
    @Symbol("sameUpstreamBranch")
    public static final class DescriptorImpl extends UpstreamPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Same branch";
        }
    }
}

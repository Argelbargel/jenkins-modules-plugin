package argelbargel.jenkins.plugins.modules;


import hudson.model.Job;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.variant.OptionalExtension;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


public class BranchUpstreamPredicate extends UpstreamPredicate {
    private static final String ANY_BRANCH = "*/*";

    private final String dependOnBranches;
    private String fallbackBranches;

    @DataBoundConstructor
    public BranchUpstreamPredicate(@Nonnull String branch) {
        dependOnBranches = branch;
        fallbackBranches = ANY_BRANCH;
    }

    @Override
    public boolean test(Job<?, ?> job) {
        BranchJobProperty branchProperty = job.getProperty(BranchJobProperty.class);
        if (branchProperty == null) {
            return fallbackBranches.equals(ANY_BRANCH);
        }

        return false;
    }


    @OptionalExtension(requirePlugins = {"branch-api", "workflow-multibranch"})
    @Symbol("branch")
    public static final class DescriptorImpl extends UpstreamPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Match branches";
        }
    }
}

package argelbargel.jenkins.plugins.modules.upstream.predicates;


import hudson.model.Job;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.variant.OptionalExtension;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

import static java.util.Optional.ofNullable;


@SuppressWarnings("unused") // extension
public class SpecificBranchUpstreamPredicate extends UpstreamPredicate {
    private String branch = "*/master";

    @DataBoundConstructor
    public SpecificBranchUpstreamPredicate() { /* NOOP */ }

    @DataBoundSetter
    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBranch() {
        return branch;
    }

    @Override
    public final boolean test(Job<?, ?> upstream, Job<?, ?> downstream) {
        return ofNullable(upstream.getProperty(BranchJobProperty.class))
                .map(BranchJobProperty::getBranch)
                .filter(b -> branch.equals(b.getName()))
                .isPresent();
    }


    @OptionalExtension(requirePlugins = {"branch-api", "workflow-multibranch"})
    @Symbol("upstreamBranch")
    public static final class DescriptorImpl extends UpstreamPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Specific branch";
        }
    }
}

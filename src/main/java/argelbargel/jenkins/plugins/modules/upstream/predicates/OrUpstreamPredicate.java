package argelbargel.jenkins.plugins.modules.upstream.predicates;


import hudson.Extension;
import hudson.model.Job;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;


@SuppressWarnings("unused") // extension
public final class OrUpstreamPredicate extends CombinedUpstreamPredicate {
    @DataBoundConstructor
    public OrUpstreamPredicate(@Nonnull List<UpstreamPredicate> predicates) {
        super(predicates);
    }

    @Override
    boolean test(Stream<UpstreamPredicate> predicates, Job<?, ?> upstream, Job<?, ?> downstream) {
        return predicates.anyMatch(p -> p.test(upstream, downstream));
    }

    @Override
    UpstreamPredicate reset(List<UpstreamPredicate> predicates) {
        return new OrUpstreamPredicate(predicates);
    }


    @Extension
    @Symbol("orUpstreamPredicate")
    public static final class DescriptorImpl extends UpstreamPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "OR";
        }
    }
}

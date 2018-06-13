package argelbargel.jenkins.plugins.modules.upstream.predicates;


import hudson.model.Job;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class CombinedUpstreamPredicate extends UpstreamPredicate {
    private List<UpstreamPredicate> predicates;

    CombinedUpstreamPredicate(@Nonnull List<UpstreamPredicate> predicates) {
        this.predicates = predicates;
    }

    public List<UpstreamPredicate> getPredicates() {
        return predicates;
    }

    @DataBoundSetter
    public void setPredicates(@Nonnull List<UpstreamPredicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public final boolean test(Job<?, ?> upstream, Job<?, ?> downstream) {
        return test(predicates.stream(), upstream, downstream);
    }

    public final int compare(Job<?, ?> upstream1, Job<?, ?> upstream2, Job<?, ?> downstream) {
        int result = 0;
        for (int i = 0; result == 0 && i < predicates.size(); ++i) {
            result = predicates.get(i).compare(upstream1, upstream2, downstream) * (predicates.size() - i);
        }

        return result;
    }

    @Override
    public final UpstreamPredicate reset() {
        return reset(predicates.stream().map(UpstreamPredicate::reset).collect(Collectors.toList()));
    }

    abstract boolean test(Stream<UpstreamPredicate> predicates, Job<?, ?> upstream, Job<?, ?> downstream);

    abstract UpstreamPredicate reset(List<UpstreamPredicate> predicates);
}

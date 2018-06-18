package argelbargel.jenkins.plugins.modules.upstream.predicates;


import com.thoughtworks.xstream.converters.UnmarshallingContext;
import hudson.Extension;
import hudson.model.Job;
import hudson.util.XStream2;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;


@SuppressWarnings("unused") // extension
public class LimitUpstreamPredicate extends AndUpstreamPredicate {
    private int limit;
    private transient int matches;

    @DataBoundConstructor
    public LimitUpstreamPredicate(List<UpstreamPredicate> predicates, int limit) {
        super(predicates);
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
    boolean test(Stream<UpstreamPredicate> predicates, Job<?, ?> upstream, Job<?, ?> downstream) {
        return super.test(predicates, upstream, downstream) && limit > matches++;
    }

    @Override
    UpstreamPredicate reset(List<UpstreamPredicate> predicates) {
        return new LimitUpstreamPredicate(predicates, limit);
    }

    @Deprecated // >= 0.9.1
    @SuppressWarnings({"deprecation", "unused"})
    public static final class ConverterImpl extends XStream2.PassthruConverter<LimitUpstreamPredicate> {
        public ConverterImpl(XStream2 xstream) {
            super(xstream);
        }

        @Override
        protected void callback(LimitUpstreamPredicate obj, UnmarshallingContext context) {
            if (obj.getPredicates() == null) {
                obj.setPredicates(emptyList());
            }
        }
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

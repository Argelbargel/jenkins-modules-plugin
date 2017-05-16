package argelbargel.jenkins.plugins.modules.predicates;


import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;


@SuppressWarnings("unused") // extension
public final class OrActionsPredicate extends CombinedActionsPredicate {
    @DataBoundConstructor
    public OrActionsPredicate(@Nonnull List<ActionsPredicate> predicates) {
        super(predicates);
    }

    @Override
    boolean test(List<ActionsPredicate> predicates, Actions reason, Actions subject) {
        for (ActionsPredicate predicate : predicates) {
            if (predicate.test(reason, subject)) {
                return true;
            }
        }

        return false;
    }


    @Extension
    @Symbol("orQueuePredicate")
    public static final class DescriptorImpl extends ActionsPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "OR";
        }
    }
}

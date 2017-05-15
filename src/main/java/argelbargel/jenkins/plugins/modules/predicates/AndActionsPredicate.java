package argelbargel.jenkins.plugins.modules.predicates;


import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;


public final class AndActionsPredicate extends CombinedActionsPredicate {
    @DataBoundConstructor
    public AndActionsPredicate(@Nonnull List<ActionsPredicate> predicates) {
        super(predicates);
    }

    @Override
    boolean test(List<ActionsPredicate> predicates, Actions reason, Actions subject) {
        for (ActionsPredicate predicate : predicates) {
            if (!predicate.test(reason, subject)) {
                return false;
            }
        }

        return true;
    }


    @Extension
    @Symbol("andQueuePredicate")
    public static final class DescriptorImpl extends ActionsPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "AND";
        }
    }
}

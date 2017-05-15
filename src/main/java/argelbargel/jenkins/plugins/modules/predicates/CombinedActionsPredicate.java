package argelbargel.jenkins.plugins.modules.predicates;


import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.List;


public abstract class CombinedActionsPredicate extends ActionsPredicate {
    private List<ActionsPredicate> predicates;

    CombinedActionsPredicate(@Nonnull List<ActionsPredicate> predicates) {
        this.predicates = predicates;
    }

    public List<ActionsPredicate> getPredicates() {
        return predicates;
    }

    @DataBoundSetter
    public void setPredicates(@Nonnull List<ActionsPredicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public final boolean test(@Nonnull Actions reason, @Nonnull Actions subject) {
        return test(predicates, reason, subject);
    }

    abstract boolean test(List<ActionsPredicate> predicates, Actions reason, Actions subject);
}

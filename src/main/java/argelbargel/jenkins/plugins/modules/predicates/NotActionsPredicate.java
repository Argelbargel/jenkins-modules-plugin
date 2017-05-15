package argelbargel.jenkins.plugins.modules.predicates;


import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;


// TODO: config.jelly
public final class NotActionsPredicate extends ActionsPredicate {
    private ActionsPredicate predicate;

    @DataBoundConstructor
    public NotActionsPredicate() {
        this.predicate = NONE;
    }

    public ActionsPredicate getPredicate() {
        return predicate;
    }

    @DataBoundSetter
    public void setPredicate(@Nonnull ActionsPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(@Nonnull Actions reason, @Nonnull Actions subject) {
        return !predicate.test(reason, subject);
    }


    @Extension
    @Symbol("notQueuePredicate")
    public static final class DescriptorImpl extends ActionsPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "NOT";
        }
    }

}

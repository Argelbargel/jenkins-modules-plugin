package argelbargel.jenkins.plugins.modules.queue.predicates;


import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;


@SuppressWarnings("unused") // extension
public class StartedWithSameBuildParametersQueuePredicate extends SingleActionQueuePredicate<ParametersAction> {
    @DataBoundConstructor
    public StartedWithSameBuildParametersQueuePredicate() {
        super(ParametersAction.class);
    }

    @Override
    protected boolean test(ParametersAction reason, ParametersAction subject) {
        return reason.getParameters().size() == subject.getParameters().size() && test(reason.getParameters(), subject);
    }

    private boolean test(List<ParameterValue> reasonParameters, ParametersAction subject) {
        for (ParameterValue reasonValue : reasonParameters) {
            if (reasonValue.getValue().equals(subject.getParameter(reasonValue.getName()))) {
                return false;
            }
        }

        return true;
    }


    @Extension
    @Symbol("startedWithSameBuildParameters")
    public static final class DescriptorImpl extends QueuePredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Started with the same parameters";
        }
    }
}

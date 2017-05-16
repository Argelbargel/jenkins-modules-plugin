package argelbargel.jenkins.plugins.modules.predicates;


import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.CauseAction;

import java.util.LinkedHashSet;
import java.util.Set;


public abstract class CauseActionPredicate<CAUSE extends Cause> extends SingleActionPredicate<CauseAction> {
    private final Class<CAUSE> causeType;
    private final boolean checkUpstream;

    @SuppressWarnings("WeakerAccess") // part of public API
    protected CauseActionPredicate(Class<CAUSE> type, boolean upstream) {
        super(CauseAction.class);
        causeType = type;
        checkUpstream = upstream;
    }

    @SuppressWarnings("unused") // used in config.jelly
    public final boolean getCheckUpstream() {
        return checkUpstream;
    }

    @Override
    protected final boolean test(CauseAction reason, CauseAction subject) {
        Set<CAUSE> reasonCauses = collectCauses(reason);
        Set<CAUSE> subjectCauses = collectCauses(subject);

        for (CAUSE reasonCause : reasonCauses) {
            for (CAUSE subjectCause : subjectCauses) {
                if (test(reasonCause, subjectCause)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected abstract boolean test(CAUSE reasonCause, CAUSE subjectCause);


    private Set<CAUSE> collectCauses(CauseAction action) {
        Set<CAUSE> causes = new LinkedHashSet<>();
        collectCauses(causes, action.getCauses(), checkUpstream);
        return causes;
    }

    private void collectCauses(Set<CAUSE> result, Iterable<Cause> causes, boolean upstream) {
        for (Cause cause : causes) {
            if (causeType.isInstance(cause)) {
                result.add(causeType.cast(cause));
            }

            if (upstream && cause instanceof UpstreamCause) {
                collectCauses(result, ((UpstreamCause) cause).getUpstreamCauses(), true);
            }
        }
    }

    @SuppressWarnings("WeakerAccess") // part of public API
    protected static abstract class CauseActionPredicateDescriptor extends ActionsPredicateDescriptor {

    }
}

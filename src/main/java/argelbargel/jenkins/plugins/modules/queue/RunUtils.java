package argelbargel.jenkins.plugins.modules.queue;


import com.google.common.base.Predicate;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;

import javax.annotation.Nullable;


final class RunUtils {
    private static final Predicate<Run<?, ?>> UNCOMPLETED_RUNS_PREDICATE = new Predicate<Run<?, ?>>() {
        @Override
        public boolean apply(@Nullable Run<?, ?> run) {
            return run != null && run.isLogUpdated();
        }
    };


    @SuppressWarnings("unchecked")
    static RunList<Run<?, ?>> getUncompletedRuns(Job<?, ?> job) {
        return ((RunList<Run<?, ?>>) job.getBuilds()).filter(UNCOMPLETED_RUNS_PREDICATE);
    }

    private RunUtils() { /* no instances required */ }
}

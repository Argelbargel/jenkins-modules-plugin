package argelbargel.jenkins.plugins.modules.predicates;


import com.dabsquared.gitlabjenkins.cause.CauseData;
import com.dabsquared.gitlabjenkins.cause.GitLabWebHookCause;
import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


@SuppressWarnings("unused") // extension
public final class StartedBySameGitlabUserPredicate extends CauseActionPredicate<GitLabWebHookCause> {
    @DataBoundConstructor
    public StartedBySameGitlabUserPredicate(boolean checkUpstream) {
        super(GitLabWebHookCause.class, checkUpstream);
    }

    @Override
    protected boolean test(GitLabWebHookCause reasonCause, GitLabWebHookCause subjectCause) {
        return test(reasonCause.getData(), subjectCause.getData());
    }

    private boolean test(CauseData reason, CauseData subject) {
        return reason.getTriggeredByUser().equals(subject.getTriggeredByUser());
    }

    @Extension(optional = true)
    @Symbol("startedBySameGitlabUserPredicate")
    public static final class DescriptorImpl extends CauseActionPredicateDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Started by a gitlab-push from the same user";
        }
    }
}

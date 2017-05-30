package argelbargel.jenkins.plugins.modules.queue.predicates;


import com.dabsquared.gitlabjenkins.cause.CauseData;
import com.dabsquared.gitlabjenkins.cause.CauseData.ActionType;
import com.dabsquared.gitlabjenkins.cause.GitLabWebHookCause;
import org.junit.Test;

import java.lang.reflect.Constructor;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;


public class StartedBySameGitlabUserPredicateTest {
    @Test
    public void testMatchesCauseTriggeredBySameUser() throws Exception {
        assertTrue(new StartedBySameGitlabUserQueuePredicate(false).test(createGitlabCause("foo"), createGitlabCause("foo")));
    }

    @Test
    public void testDoesNotMatchCauseTriggeredByDifferentUser() throws Exception {
        assertFalse(new StartedBySameGitlabUserQueuePredicate(false).test(createGitlabCause("foo"), createGitlabCause("bar")));
    }


    private GitLabWebHookCause createGitlabCause(String user) throws Exception {
        return new GitLabWebHookCause(createCauseData(user));
    }

    private CauseData createCauseData(String user) throws Exception {
        Constructor<CauseData> constructor = makeCauseDataConstructorAccessible();
        return constructor.newInstance(
                ActionType.PUSH,
                0, 0,
                "master", "master",
                "", "",
                "", "", "", "", "", "",
                "", "", 0, 0,
                "master",
                "", "", "", "",
                user,
                "", "", "", "", ""
        );
    }

    private Constructor<CauseData> makeCauseDataConstructorAccessible() throws NoSuchMethodException {
        Constructor<CauseData> constructor = CauseData.class.getDeclaredConstructor(
                ActionType.class, Integer.class, Integer.class, String.class, String.class, String.class,
                String.class, String.class, String.class, String.class, String.class,
                String.class, String.class, String.class, String.class, Integer.class,
                Integer.class, String.class, String.class, String.class, String.class,
                String.class, String.class, String.class, String.class, String.class, String.class,
                String.class
        );
        constructor.setAccessible(true);
        return constructor;
    }
}
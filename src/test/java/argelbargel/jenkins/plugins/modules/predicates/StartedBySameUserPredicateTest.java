package argelbargel.jenkins.plugins.modules.predicates;


import argelbargel.jenkins.plugins.modules.MockUtils;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Cause.UserIdCause;
import hudson.model.CauseAction;
import hudson.model.Run;
import hudson.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static argelbargel.jenkins.plugins.modules.MockUtils.mockUsers;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;


@RunWith(PowerMockRunner.class)
@PrepareForTest({User.class})
public class StartedBySameUserPredicateTest {

    @Test
    public void testMatchesSameUser() throws Exception {
        mockUsers("foo", "foo");
        UserIdCause reason = new UserIdCause();
        UserIdCause subject = new UserIdCause();

        assertTrue(new StartedBySameUserPredicate(false).test(reason, subject));
    }

    @Test
    public void testDoesNotMatchDifferentUser() throws Exception {
        mockUsers("foo", "bar");
        UserIdCause reason = new UserIdCause();
        UserIdCause subject = new UserIdCause();

        assertFalse(new StartedBySameUserPredicate(false).test(reason, subject));
    }

    @Test
    public void testWalksUpstream() throws Exception {
        mockUsers("foo", "foo");
        UserIdCause userCause1 = new UserIdCause();
        UserIdCause userCause2 = new UserIdCause();

        Run run = MockUtils.mockRun(userCause2);
        UpstreamCause upstreamCause = new UpstreamCause(run);

        StartedBySameUserPredicate underTest = new StartedBySameUserPredicate(true);

        assertTrue(underTest.test(new CauseAction(userCause1), new CauseAction(upstreamCause)));
        assertTrue(underTest.test(new CauseAction(upstreamCause), new CauseAction(userCause1)));
    }

}
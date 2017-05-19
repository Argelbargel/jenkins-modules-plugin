package argelbargel.jenkins.plugins.modules.predicates;


import hudson.model.Cause.UpstreamCause;
import hudson.model.Cause.UserIdCause;
import hudson.model.CauseAction;
import hudson.model.Run;
import hudson.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static argelbargel.jenkins.plugins.modules.MockUtils.mockRun;
import static argelbargel.jenkins.plugins.modules.MockUtils.mockUsers;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(PowerMockRunner.class)
@PrepareForTest({User.class})
public class UpstreamBuildPredicateTest {
    @Test
    public void testMatchesSameUpstreamProject() {
        Run<?, ?> run1 = mockRun("test1", 1);
        Run<?, ?> run2 = mockRun("test1", 1);

        assertTrue(new UpstreamBuildPredicate().test(new UpstreamCause(run1), new UpstreamCause(run2)));
    }

    @Test
    public void testDoesNotMatchUpstreamProjectWithDifferentNames() {
        Run<?, ?> run1 = mockRun("test1", 1);
        Run<?, ?> run2 = mockRun("test2", 1);

        assertFalse(new UpstreamBuildPredicate().test(new UpstreamCause(run1), new UpstreamCause(run2)));
    }

    @Test
    public void testDoesNotMatchUpstreamProjectWithDifferentBuildNumbers() {
        Run<?, ?> run1 = mockRun("test1", 1);
        Run<?, ?> run2 = mockRun("test1", 2);

        assertFalse(new UpstreamBuildPredicate().test(new UpstreamCause(run1), new UpstreamCause(run2)));
    }

    @Test
    public void testDoesNotMatchUpstreamProjectWithDifferentCauses() throws Exception {
        mockUsers("foo", "bar");

        Run<?, ?> run1 = mockRun("test1", 1, new UserIdCause());
        Run<?, ?> run2 = mockRun("test1", 2, new UserIdCause());

        assertFalse(new UpstreamBuildPredicate().test(new UpstreamCause(run1), new UpstreamCause(run2)));
    }

    @Test
    public void testWalksUpstreamCauses() {
        Run<?, ?> run1 = mockRun("test1", 1, new UserIdCause());
        Run<?, ?> run2 = mockRun("test1", 1, new UserIdCause());
        Run<?, ?> run3 = mockRun("upstream", 2, new UpstreamCause(run2));

        CauseAction action1 = new CauseAction(new UpstreamCause(run1));
        CauseAction action2 = new CauseAction(new UpstreamCause(run3));

        assertTrue(new UpstreamBuildPredicate().test(action1, action2));
        assertTrue(new UpstreamBuildPredicate().test(action2, action1));
    }
}
package argelbargel.jenkins.plugins.modules.queue.predicates;


import hudson.model.Action;
import hudson.model.Cause.UserIdCause;
import hudson.model.CauseAction;
import hudson.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;

import static argelbargel.jenkins.plugins.modules.MockUtils.mockUsers;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;


@RunWith(PowerMockRunner.class)
@PrepareForTest({User.class})
public class OrQueuePredicateTest {
    @Test
    public void testMatchesForAnyMatchingPredicate() throws Exception {
        mockUsers("foo");

        List<Action> actions = Collections.<Action>singletonList(new CauseAction(new UserIdCause()));
        Actions reasons = Actions.create(actions);
        Actions subject = Actions.create(actions);

        QueuePredicate predicate1 = new StartedByUserQueuePredicate("bar", false);
        QueuePredicate predicate2 = new StartedByUserQueuePredicate("foo", false);

        assertTrue(new OrQueuePredicate(asList(predicate1, predicate2)).test(reasons, subject));
        assertTrue(new OrQueuePredicate(asList(predicate2, predicate1)).test(reasons, subject));
    }

    @Test
    public void testMatchesOnlyIfAPredicateMatches() throws Exception {
        mockUsers("foo");

        List<Action> actions = Collections.<Action>singletonList(new CauseAction(new UserIdCause()));
        Actions reasons = Actions.create(actions);
        Actions subject = Actions.create(actions);

        QueuePredicate predicate1 = new StartedByUserQueuePredicate("bar", false);

        assertFalse(new OrQueuePredicate(singletonList(predicate1)).test(reasons, subject));
    }
}
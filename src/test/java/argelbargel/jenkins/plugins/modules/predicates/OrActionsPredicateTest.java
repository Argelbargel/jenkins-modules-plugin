package argelbargel.jenkins.plugins.modules.predicates;


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
public class OrActionsPredicateTest {
    @Test
    public void testMatchesForAnyMatchingPredicate() throws Exception {
        mockUsers("foo");

        List<Action> actions = Collections.<Action>singletonList(new CauseAction(new UserIdCause()));
        Actions reasons = Actions.create(actions);
        Actions subject = Actions.create(actions);

        ActionsPredicate predicate1 = new StartedByUserPredicate("bar", false);
        ActionsPredicate predicate2 = new StartedByUserPredicate("foo", false);

        assertTrue(new OrActionsPredicate(asList(predicate1, predicate2)).test(reasons, subject));
        assertTrue(new OrActionsPredicate(asList(predicate2, predicate1)).test(reasons, subject));
    }

    @Test
    public void testMatchesOnlyIfAPredicateMatches() throws Exception {
        mockUsers("foo");

        List<Action> actions = Collections.<Action>singletonList(new CauseAction(new UserIdCause()));
        Actions reasons = Actions.create(actions);
        Actions subject = Actions.create(actions);

        ActionsPredicate predicate1 = new StartedByUserPredicate("bar", false);

        assertFalse(new OrActionsPredicate(singletonList(predicate1)).test(reasons, subject));
    }
}
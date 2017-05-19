package argelbargel.jenkins.plugins.modules;


import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.User;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


public final class MockUtils {
    public static void mockUsers(String... users) throws Exception {
        final LinkedList<String> queue = new LinkedList<>(asList(users));
        final Constructor<User> newUser = User.class.getConstructor(String.class, String.class);
        newUser.setAccessible(true);

        prepareUserClass();
        when(User.current()).thenAnswer(new Answer<User>() {
            @Override
            public User answer(InvocationOnMock invocation) throws Throwable {
                String userId = queue.pop();
                return newUser.newInstance(userId, userId);
            }
        });

    }

    public static Job mockJob(String fullName) {
        ItemGroup root = mockRoot();
        Job job = mock(Job.class);
        when(job.getParent()).thenReturn(root);
        when(job.getFullName()).thenReturn(fullName);
        when(job.getUrl()).thenReturn("job/" + fullName);
        return job;
    }

    public static Run mockRun(Cause... causes) {
        return mockRun("test", 1, causes);
    }

    public static Run mockRun(String jobFullName, int number, Cause... causes) {
        Job job = mockJob(jobFullName);

        Run run = mock(Run.class);
        when(run.getParent()).thenReturn(job);
        when(run.getNumber()).thenReturn(number);
        when(run.getAction(ArgumentMatchers.<Class<? extends Action>>any())).thenCallRealMethod();
        when(run.getAllActions()).thenCallRealMethod();
        doCallRealMethod().when(run).addAction(ArgumentMatchers.<Action>any());
        when(run.getCauses()).thenCallRealMethod();
        run.addAction(new CauseAction(asList(causes)));
        return run;
    }


    private static ItemGroup mockRoot() {
        ItemGroup root = mock(ItemGroup.class);
        when(root.getFullName()).thenReturn("");
        when(root.getUrl()).thenReturn("/");
        return root;
    }

    private static void prepareUserClass() throws NoSuchMethodException {
        final Method load = User.class.getDeclaredMethod("load");
        mockStatic(User.class);
        PowerMockito.suppress(load);
    }

    private MockUtils() { /* no instances required */ }

}

package argelbargel.jenkins.plugins.modules;


import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.Queue.Item;
import hudson.model.Run;

import java.util.List;


public abstract class QueueData {
    static QueueData dataForItem(final Item item) {
        return new QueueData() {
            @Override
            public <A extends Action> A getAction(Class<A> type) {
                return item.getAction(type);
            }

            @Override
            public <A extends Action> List<A> getActions(Class<A> type) {
                return item.getActions(type);
            }

            @Override
            public List<? extends Action> getAllActions() {
                return item.getAllActions();
            }

            @Override
            public List<Cause> getCauses() {
                return item.getCauses();
            }

            @Override
            public <C extends Cause> C getCause(Class<C> type) {
                for (Cause c : getCauses()) {
                    if (type.isInstance(c)) {
                        return type.cast(c);
                    }
                }

                return null;

            }
        };
    }

    static QueueData dataForRun(final Run<?, ?> run) {
        return new QueueData() {
            @Override
            public <A extends Action> A getAction(Class<A> type) {
                return run.getAction(type);
            }

            @Override
            public <A extends Action> List<A> getActions(Class<A> type) {
                return run.getActions(type);
            }

            @Override
            public List<? extends Action> getAllActions() {
                return run.getAllActions();
            }

            @Override
            public List<? extends Cause> getCauses() {
                return run.getCauses();
            }

            @Override
            public <C extends Cause> C getCause(Class<C> type) {
                return run.getCause(type);
            }
        };
    }

    public abstract <A extends Action> A getAction(Class<A> type);

    public abstract <A extends Action> List<A> getActions(Class<A> type);

    public abstract List<? extends Action> getAllActions();

    public abstract List<? extends Cause> getCauses();

    public abstract <C extends Cause> C getCause(Class<C> type);

    private QueueData() { /* no instances allowed */ }
}

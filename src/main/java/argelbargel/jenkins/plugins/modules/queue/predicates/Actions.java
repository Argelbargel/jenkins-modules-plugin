package argelbargel.jenkins.plugins.modules.queue.predicates;


import hudson.model.Action;
import hudson.model.Actionable;

import java.util.List;


@SuppressWarnings("WeakerAccess") // part of public api
public abstract class Actions {
    static Actions create(final Actionable actionable) {
        return new Actions() {
            @Override
            public List<? extends Action> getAllActions() {
                return actionable.getAllActions();
            }
        };
    }

    static Actions create(final List<Action> actions) {
        return new Actions() {
            @Override
            public List<? extends Action> getAllActions() {
                return actions;
            }
        };
    }

    abstract List<? extends Action> getAllActions();

    final <ACTION extends Action> ACTION getAction(Class<ACTION> type) {
        for (Action action : getAllActions()) {
            if (type.isInstance(action)) {
                return type.cast(action);
            }
        }

        return null;
    }

    private Actions() { /* no instances outside package allowed */ }
}

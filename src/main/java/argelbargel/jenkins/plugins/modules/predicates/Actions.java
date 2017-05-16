package argelbargel.jenkins.plugins.modules.predicates;


import hudson.Util;
import hudson.model.Action;
import hudson.model.Actionable;

import java.util.List;


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

    public abstract List<? extends Action> getAllActions();

    @SuppressWarnings("unused") // part of public API
    public final <ACTION extends Action> List<ACTION> getActions(Class<ACTION> type) {
        return Util.filter(getAllActions(), type);
    }

    @SuppressWarnings("WeakerAccess") // part of public API
    public final <ACTION extends Action> ACTION getAction(Class<ACTION> type) {
        for (Action action : getAllActions()) {
            if (type.isInstance(action)) {
                return type.cast(action);
            }
        }

        return null;
    }

    private Actions() { /* no instances outside package allowed */ }
}

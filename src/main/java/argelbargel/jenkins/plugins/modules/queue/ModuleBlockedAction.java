package argelbargel.jenkins.plugins.modules.queue;


import com.google.common.base.Predicate;
import hudson.model.Actionable;
import hudson.model.InvisibleAction;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Queue.Item;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.Stack;


public final class ModuleBlockedAction extends InvisibleAction {
    public static ModuleBlockedAction get(Actionable actionable) {
        return actionable.getAction(ModuleBlockedAction.class);
    }

    static void block(Item item, Item reason) {
        block(item, reason.getId(), ((Job) reason.task).getFullName(), null, ((Job) reason.task).getParent().getUrl());
    }

    static void block(Item item, Run reason) {
        block(item, reason.getQueueId(), reason.getParent().getFullName(), reason.getNumber(), reason.getParent().getUrl());
    }

    private static void block(Item item, long id, String name, Integer build, String url) {
        ModuleBlockedAction action = item.getAction(ModuleBlockedAction.class);
        if (action == null) {
            action = new ModuleBlockedAction();
            item.addAction(action);
        }

        if (!action.isBlockedBy(id)) {
            action.block(id, name, build, url);
        }
    }

    static void cancelItemsBlockedBy(Item reason) {
        cancelItemsBy(new BlockedByPredicate<Item>(reason) {
            @Override
            protected boolean isBlockedBy(ModuleBlockedAction blocked, Item item) {
                return blocked.isBlockedBy(item.getId());
            }
        });
    }

    static void cancelItemsBlockedBy(Run reason) {
        cancelItemsBy(new BlockedByPredicate<Run>(reason) {
            @Override
            protected boolean isBlockedBy(ModuleBlockedAction blocked, Run run) {
                return blocked.isBlockedBy(run.getQueueId());
            }
        });
    }

    private static void cancelItemsBy(Predicate<Item> predicate) {
        Queue queue = Jenkins.getInstance().getQueue();
        for (Item item : queue.getItems()) {
            if (predicate.apply(item)) {
                queue.cancel(item);
            }
        }
    }

    private final Stack<Blocker> blockers;
    private long blockedTotalDuration;

    private ModuleBlockedAction() {
        blockers = new Stack<>();
        blockedTotalDuration = 0;
    }

    private void block(long id, String name, Integer build, String url) {
        unblock();
        blockers.push(new Blocker(id, name, build, url));
    }

    public boolean hasBeenBlockedBy(Run run) {
        return hasBeenBlockedBy(run.getQueueId(), run.getNumber());
    }

    // use queue-id and build-number so we survive restarts
    private boolean hasBeenBlockedBy(long id, int number) {
        for (Blocker blocker : blockers) {
            if (blocker.id() == id && blocker.getBuild() == number) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unused") // used by summary.jelly
    public long getTotalBlockDuration() {
        return blockedTotalDuration;
    }

    @SuppressWarnings("unused") // used by summary.jelly
    public List<Blocker> getBlockers() {
        return blockers;
    }

    private boolean isBlockedBy(long queueId) {
        return !blockers.isEmpty() && blockers.peek().id() == queueId;
    }

    void unblock() {
        if (!blockers.isEmpty()) {
            blockedTotalDuration += blockers.peek().unblock();
        }
    }


    private static abstract class BlockedByPredicate<REASON> implements Predicate<Item> {
        private final REASON reason;

        BlockedByPredicate(REASON reason) {
            this.reason = reason;
        }

        @Override
        public boolean apply(Item input) {
            if (!input.isBlocked()) {
                return false;
            }

            ModuleBlockedAction blocked = input.getAction(ModuleBlockedAction.class);
            return blocked != null && isBlockedBy(blocked, reason);
        }

        protected abstract boolean isBlockedBy(ModuleBlockedAction blocked, REASON reason);
    }
}

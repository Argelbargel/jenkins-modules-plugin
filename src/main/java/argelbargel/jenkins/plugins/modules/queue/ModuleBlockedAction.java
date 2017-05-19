package argelbargel.jenkins.plugins.modules.queue;


import com.google.common.base.Predicate;
import hudson.Util;
import hudson.model.Actionable;
import hudson.model.InvisibleAction;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Queue.Item;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.System.currentTimeMillis;


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

        action.block(id, name, build, url);
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

    private final Map<Long, Blocker> blockers;
    private long blockStart;
    private Long blockEnd;

    private ModuleBlockedAction() {
        blockers = new LinkedHashMap<>();
        blockStart = currentTimeMillis();
        blockEnd = null;
    }

    public boolean wasBlockedBy(Run run) {
        return wasBlockedBy(run.getQueueId(), run.getNumber());
    }

    public boolean isBlocked() {
        return blockEnd != null;
    }

    @SuppressWarnings("unused") // used by summary.jelly
    public String getBlockDuration() {
        return Util.getTimeSpanString(blockEnd != null ? blockEnd - blockStart : currentTimeMillis() - blockStart);
    }

    @SuppressWarnings("unused") // used by summary.jelly
    public Collection<Blocker> getBlockers() {
        return blockers.values();
    }

    void unblock() {
        blockEnd = currentTimeMillis();
    }

    private void block(long id, String name, Integer build, String url) {
        if (!blockers.containsKey(id)) {
            blockers.put(id, new Blocker(name, build, url));
        }
    }

    // use queue-id and build-number so we still show correct data after restarts
    private boolean wasBlockedBy(long queueId, int build) {
        return blockers.containsKey(queueId) && blockers.get(queueId).getBuild() == build;
    }

    private boolean isBlockedBy(long queueId) {
        return blockEnd != null && blockers.containsKey(queueId);
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

package argelbargel.jenkins.plugins.modules.queue;


import argelbargel.jenkins.plugins.modules.ModuleAction;
import com.google.common.base.Predicate;
import hudson.Util;
import hudson.model.Actionable;
import hudson.model.InvisibleAction;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Queue.Item;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.System.currentTimeMillis;


public final class ModuleBlockedAction extends InvisibleAction {
    public static ModuleBlockedAction get(Actionable actionable) {
        return actionable.getAction(ModuleBlockedAction.class);
    }

    static void block(Item item, Item reason) {
        block(item, reason.getId(), (Job) reason.task, null);
    }

    static void block(Item item, Run reason) {
        block(item, reason.getQueueId(), reason.getParent(), reason.getNumber());
    }

    private static void block(Item item, long id, Job<?, ?> task, Integer build) {
        ModuleBlockedAction action = item.getAction(ModuleBlockedAction.class);
        if (action == null) {
            action = new ModuleBlockedAction();
            item.addAction(action);
        }

        action.block(id, task, build);
    }

    static void cancelItemsBlockedBy(Item reason) {
        cancelItemsBy(new BlockedByPredicate(reason.getId()));
    }

    static void cancelItemsBlockedBy(Run reason) {
        cancelItemsBy(new BlockedByPredicate(reason.getQueueId()));
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
        return blockEnd == null;
    }

    @SuppressWarnings("unused") // used by summary.jelly
    public String getBlockDuration() {
        return Util.getTimeSpanString(blockEnd != null ? blockEnd - blockStart : currentTimeMillis() - blockStart);
    }

    @SuppressWarnings({"unused", "WeakerAccess"}) // used by summary.jelly
    public Collection<Blocker> getAllBlockers() {
        return blockers.values();
    }

    public Collection<Blocker> getCurrentBlockers() {
        Collection<Blocker> current = new ArrayList<>();
        for (Blocker blocker : getAllBlockers()) {
            if (blocker.isBlocking()) {
                current.add(blocker);
            }
        }

        return current;
    }

    void unblock() {
        blockEnd = currentTimeMillis();
    }

    private void block(long queueId, Job task, Integer build) {
        blockers.put(queueId, new Blocker(queueId, ModuleAction.get(task).getModuleName(), task.getFullName(), build, task.getUrl()));
    }

    // use queue-id and build-number so we still show correct data after restarts
    private boolean wasBlockedBy(long queueId, int build) {
        return blockers.containsKey(queueId) && blockers.get(queueId).getBuild() == build;
    }

    boolean isBlockedBy(long queueId) {
        return blockEnd == null && blockers.containsKey(queueId);
    }
}

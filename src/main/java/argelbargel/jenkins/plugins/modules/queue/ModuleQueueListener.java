package argelbargel.jenkins.plugins.modules.queue;


import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Queue.BlockedItem;
import hudson.model.Queue.Item;
import hudson.model.Queue.LeftItem;
import hudson.model.queue.QueueListener;


@Extension
public final class ModuleQueueListener extends QueueListener {
    @Override
    public void onLeaveBlocked(BlockedItem blocked) {
        ModuleBlockedAction state = ModuleBlockedAction.get(blocked);
        if (state != null) {
            state.unblock();
        }
    }

    @Override
    public void onLeft(LeftItem left) {
        if (left.isCancelled()) {
            ModuleBlockedAction.cancelItemsBlockedBy(left);
        }
    }

    private boolean hasProjectTask(Item item) {
        return item.task instanceof AbstractProject;
    }
}

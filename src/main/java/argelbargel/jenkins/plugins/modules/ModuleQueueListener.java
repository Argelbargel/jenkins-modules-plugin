package argelbargel.jenkins.plugins.modules;


import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Queue.BlockedItem;
import hudson.model.Queue.Item;
import hudson.model.Queue.LeftItem;
import hudson.model.Queue.WaitingItem;
import hudson.model.queue.QueueListener;


@Extension
public final class ModuleQueueListener extends QueueListener {
    @Override
    public void onEnterWaiting(WaitingItem waiting) {
        if (hasProjectTask(waiting)) {
            onEnterWaiting((AbstractProject) waiting.task);
        }
    }

    private void onEnterWaiting(AbstractProject<?, ?> task) {
        Module module = task.getTrigger(Module.class);
        if (module != null && QueueUtils.hasDownstream(task)) {
            Registry.registry().start(module.getName(), task);
        }
    }

    @Override
    public void onEnterBlocked(BlockedItem blocked) {
        if (hasProjectTask(blocked)) {
            onEnterBlocked((AbstractProject) blocked.task, blocked);
        }
    }

    private void onEnterBlocked(AbstractProject<?, ?> project, BlockedItem blocked) {
        Module module = project.getTrigger(Module.class);
        if (module != null && QueueUtils.hasUpstream(project)) {
            Registry.registry().block(project, blocked);
        }
    }

    @Override
    public void onLeaveBlocked(BlockedItem blocked) {
        if (hasProjectTask(blocked)) {
            onLeaveBlocked((AbstractProject) blocked.task, blocked);
        }
    }

    private void onLeaveBlocked(AbstractProject project, BlockedItem blocked) {
        if (Registry.registry().isBlocked(project)) {
            Registry.registry().unblock(project, blocked);
        }
    }

    @Override
    public void onLeft(LeftItem left) {
        if (left.isCancelled() && hasProjectTask(left)) {
            onCanceled(left, (AbstractProject) left.task);
        }
    }

    private void onCanceled(LeftItem canceled, AbstractProject<?, ?> task) {
        Module module = task.getTrigger(Module.class);
        if (module != null && QueueUtils.hasDownstream(task)) {
            QueueUtils.cancelDownstreamForItem(canceled, task, module.getPredicate());
            Registry.registry().stop(module.getName());
        }
    }

    private boolean hasProjectTask(Item item) {
        return item.task instanceof AbstractProject;
    }
}

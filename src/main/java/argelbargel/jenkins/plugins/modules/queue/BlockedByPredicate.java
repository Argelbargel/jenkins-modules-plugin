package argelbargel.jenkins.plugins.modules.queue;


import com.google.common.base.Predicate;
import hudson.model.Queue;


class BlockedByPredicate implements Predicate<Queue.Item> {
    private final long reasonQueueId;

    BlockedByPredicate(long id) {
        this.reasonQueueId = id;
    }

    @Override
    public boolean apply(Queue.Item input) {
        if (!input.isBlocked() || input.getId() == reasonQueueId) {
            return false;
        }

        ModuleBlockedAction blocked = input.getAction(ModuleBlockedAction.class);
        return blocked != null && blocked.isBlockedBy(reasonQueueId);
    }
}

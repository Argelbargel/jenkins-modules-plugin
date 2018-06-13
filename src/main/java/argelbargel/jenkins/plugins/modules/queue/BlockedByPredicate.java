package argelbargel.jenkins.plugins.modules.queue;


import hudson.model.Queue;

import java.util.function.Predicate;


class BlockedByPredicate implements Predicate<Queue.Item> {
    private final long reasonQueueId;

    BlockedByPredicate(long id) {
        this.reasonQueueId = id;
    }

    @Override
    public boolean test(Queue.Item input) {
        if (input.getId() == reasonQueueId || !input.isBlocked()) {
            return false;
        }

        ModuleBlockedAction blocked = input.getAction(ModuleBlockedAction.class);
        return blocked != null && blocked.isBlockedBy(reasonQueueId);
    }
}

package argelbargel.jenkins.plugins.modules;


import argelbargel.jenkins.plugins.modules.predicates.QueueDataPredicate;
import hudson.model.AbstractProject;
import hudson.model.Queue.Item;
import hudson.model.Run;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.util.Collection;

import static argelbargel.jenkins.plugins.modules.QueueData.dataForItem;
import static argelbargel.jenkins.plugins.modules.QueueData.dataForRun;
import static argelbargel.jenkins.plugins.modules.Registry.registry;


class QueueUtils {
    static Run<?, ?> findBlockingRun(QueueDataPredicate predicate, Item waiting, Iterable<Run<?, ?>> items) {
        return findBlocking(adaptPredicateForRunSubject(predicate), waiting, items);
    }

    static Item findBlockingItem(QueueDataPredicate predicate, Item waiting, Iterable<Item> items) {
        return findBlocking(adaptPredicateForItems(predicate), waiting, items);
    }

    static void cancelDownstreamForRun(Run<?, ?> reason, AbstractProject project, QueueDataPredicate predicate) {
        cancelDownstream(reason, project, adaptPredicateForRunReason(predicate));
    }

    static void cancelDownstreamForItem(Item reason, AbstractProject project, QueueDataPredicate predicate) {
        cancelDownstream(reason, project, adaptPredicateForItems(predicate));
    }

    static boolean hasDownstream(AbstractProject project) {
        return !project.getDownstreamProjects().isEmpty();
    }

    static boolean hasUpstream(AbstractProject project) {
        return !project.getUpstreamProjects().isEmpty();
    }

    private static <SUBJECT> SUBJECT findBlocking(PredicateAdapter<Item, SUBJECT> predicate, Item waiting, Iterable<SUBJECT> items) {
        for (SUBJECT subject : items) {
            if (predicate.test(waiting, subject)) {
                return subject;
            }
        }

        return null;
    }

    private static <REASON> void cancelDownstream(REASON reason, AbstractProject<?, ?> project, PredicateAdapter<REASON, Item> predicate) {
        for (AbstractProject downstreamProject : project.getDownstreamProjects()) {
            if (registry().isBlocked(downstreamProject)) {
                cancelItems(reason, registry().blocked(downstreamProject), predicate);
            }
        }
    }

    private static <REASON> void cancelItems(REASON reason, Collection<Item> blockedItems, PredicateAdapter<REASON, Item> predicate) {
        for (Item blocked : blockedItems) {
            if (predicate.test(reason, blocked)) {
                Jenkins.getInstance().getQueue().cancel(blocked);
            }
        }
    }

    private static PredicateAdapter<Run<?, ?>, Item> adaptPredicateForRunReason(QueueDataPredicate predicate) {
        return new PredicateAdapter<Run<?, ?>, Item>(predicate) {
            @Override
            boolean test(Run<?, ?> run, Item item) {
                return test(dataForRun(run), dataForItem(item));
            }
        };
    }

    private static PredicateAdapter<Item, Item> adaptPredicateForItems(QueueDataPredicate predicate) {
        return new PredicateAdapter<Item, Item>(predicate) {
            @Override
            boolean test(Item reason, Item subject) {
                return test(dataForItem(reason), dataForItem(subject));
            }
        };
    }

    private static PredicateAdapter<Item, Run<?, ?>> adaptPredicateForRunSubject(QueueDataPredicate predicate) {
        return new PredicateAdapter<Item, Run<?, ?>>(predicate) {
            @Override
            boolean test(Item reason, Run<?, ?> subject) {
                return test(dataForItem(reason), dataForRun(subject));
            }
        };
    }

    private QueueUtils() { /* no instances allowed */ }


    private static abstract class PredicateAdapter<REASON, SUBJECT> extends QueueDataPredicate {
        private final QueueDataPredicate delegate;

        PredicateAdapter(QueueDataPredicate delegate) {
            this.delegate = delegate;
        }

        abstract boolean test(REASON reason, SUBJECT subject);

        @Override
        public final boolean test(@Nonnull QueueData reason, @Nonnull QueueData subject) {
            return delegate.test(reason, subject);
        }
    }
}

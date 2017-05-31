package argelbargel.jenkins.plugins.modules.views;


import argelbargel.jenkins.plugins.modules.Messages;
import argelbargel.jenkins.plugins.modules.queue.Blocker;
import argelbargel.jenkins.plugins.modules.queue.ModuleBlockedAction;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Queue.Item;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;


@SuppressWarnings("unused") // extension
public final class BlockedColumn extends ListViewColumn {
    @DataBoundConstructor
    public BlockedColumn() {
        super();
    }

    public Collection<Collection<Blocker>> getBlockers(Job<?, ?> job) {
        // order blocked items by queue-id
        Map<Long, Collection<Blocker>> blockers = new TreeMap<>();

        for (Item item : Jenkins.getInstance().getQueue().getItems()) {
            if (item.task == job) {
                ModuleBlockedAction blocked = ModuleBlockedAction.get(item);
                if (blocked != null && blocked.isBlocked()) {
                    blockers.put(item.getId(), blocked.getCurrentBlockers());
                }
            }
        }

        return blockers.values();
    }

    @Extension(ordinal = DEFAULT_COLUMNS_ORDINAL_PROPERTIES_START - 3)
    @Symbol("module")
    public static final class DescriptorImpl extends ListViewColumnDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.BlockedColumn_DisplayName();
        }

        @Override
        public boolean shownByDefault() {
            return false;
        }
    }
}

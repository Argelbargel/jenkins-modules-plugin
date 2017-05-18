package argelbargel.jenkins.plugins.modules.views;


import argelbargel.jenkins.plugins.modules.Messages;
import argelbargel.jenkins.plugins.modules.ModuleAction;
import argelbargel.jenkins.plugins.modules.queue.ModuleBlockedAction;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Actionable;
import hudson.model.Descriptor;
import hudson.model.Queue;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.util.ListBoxModel;
import hudson.views.ViewJobFilter;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;

import static argelbargel.jenkins.plugins.modules.views.ModuleViewFilter.Mode.ALL;
import static argelbargel.jenkins.plugins.modules.views.ModuleViewFilter.Mode.IGNORE_BLOCKED;


@SuppressWarnings("unused") // extension
public final class ModuleViewFilter extends ViewJobFilter {
    enum Mode {
        ALL(Messages.ModuleViewFilter_Mode_ShowAllModules()),
        BLOCKED_ONLY(Messages.ModuleViewFilter_Mode_BlockedOnly()),
        IGNORE_BLOCKED(Messages.ModuleViewFilter_Mode_IgnoreBlocked());

        private final String displayName;

        Mode(String name) {
            this.displayName = name;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final Mode mode;

    @DataBoundConstructor
    public ModuleViewFilter(String mode) {
        this.mode = Mode.valueOf(mode);
    }

    public String getMode() {
        return mode.name();
    }

    @Override
    public List<TopLevelItem> filter(List<TopLevelItem> added, List<TopLevelItem> all, View filteringView) {
        for (TopLevelItem item : all) {
            if (item instanceof Actionable && ((Actionable) item).getAction(ModuleAction.class) != null) {
                if (!accept(item)) {
                    added.remove(item);
                } else if (!added.contains(item)) {
                    added.add(item);
                }
            }
        }

        return added;
    }

    @SuppressFBWarnings
    private boolean accept(TopLevelItem item) {
        return ALL.equals(mode) || (IGNORE_BLOCKED.equals(mode) || isBlocked(item));
    }

    private boolean isBlocked(TopLevelItem item) {
        for (Queue.Item queued : Jenkins.getInstance().getQueue().getItems()) {
            if (queued.task == item) {
                ModuleBlockedAction blocked = ModuleBlockedAction.get(queued);
                return blocked != null && blocked.isBlocked();
            }
        }
        return false;
    }

    @Extension
    public final static class DescriptorImpl extends Descriptor<ViewJobFilter> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ModuleViewFilter_DisplayName();
        }

        public ListBoxModel doFillModeItems() {
            ListBoxModel model = new ListBoxModel();
            for (Mode mode : Mode.values()) {
                model.add(mode.getDisplayName(), mode.name());
            }

            return model;
        }
    }
}

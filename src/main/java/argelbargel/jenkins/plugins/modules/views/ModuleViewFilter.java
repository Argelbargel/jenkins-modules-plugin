package argelbargel.jenkins.plugins.modules.views;


import argelbargel.jenkins.plugins.modules.Messages;
import argelbargel.jenkins.plugins.modules.ModuleAction;
import argelbargel.jenkins.plugins.modules.queue.ModuleBlockedAction;
import hudson.Extension;
import hudson.model.Actionable;
import hudson.model.Descriptor;
import hudson.model.Queue;
import hudson.model.TopLevelItem;
import hudson.views.AbstractIncludeExcludeJobFilter;
import hudson.views.ViewJobFilter;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


@SuppressWarnings("unused") // extension
public final class ModuleViewFilter extends AbstractIncludeExcludeJobFilter {
    private boolean matchOnlyBlocked;

    @DataBoundConstructor
    public ModuleViewFilter(String includeExcludeTypeString, boolean matchOnlyBlocked) {
        super(includeExcludeTypeString);
        this.matchOnlyBlocked = matchOnlyBlocked;
    }

    public boolean getMatchOnlyBlocked() {
        return matchOnlyBlocked;
    }

    @Override
    protected boolean matches(TopLevelItem item) {
        return isModule(item) && (!matchOnlyBlocked || isBlocked(item));

    }

    private boolean isModule(TopLevelItem item) {
        return item instanceof Actionable && ((Actionable) item).getAction(ModuleAction.class) != null;
    }

    private boolean isBlocked(TopLevelItem item) {
        for (Queue.Item queued : Jenkins.get().getQueue().getItems()) {
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
    }
}

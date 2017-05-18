package argelbargel.jenkins.plugins.modules.views;


import argelbargel.jenkins.plugins.modules.Messages;
import argelbargel.jenkins.plugins.modules.ModuleAction;
import hudson.Extension;
import hudson.model.Actionable;
import hudson.model.Descriptor;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.views.ViewJobFilter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;


@SuppressWarnings("unused") // extension
public final class ModuleViewFilter extends ViewJobFilter {
    @DataBoundConstructor
    public ModuleViewFilter() {
        super();
    }

    @Override
    public List<TopLevelItem> filter(List<TopLevelItem> added, List<TopLevelItem> all, View filteringView) {
        for (TopLevelItem item : all) {
            if (item instanceof Actionable) {
                if (((Actionable) item).getAction(ModuleAction.class) == null) {
                    added.remove(item);
                } else if (!added.contains(item)) {
                    added.add(item);
                }
            }
        }

        return added;
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

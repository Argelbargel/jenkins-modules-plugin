package argelbargel.jenkins.plugins.modules.views;


import argelbargel.jenkins.plugins.modules.ModuleAction;
import argelbargel.jenkins.plugins.modules.graph.ModuleGraphJobAction;
import hudson.Extension;
import hudson.model.Job;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;


@SuppressWarnings("unused") // extension
public final class ModuleColumn extends ListViewColumn {
    @DataBoundConstructor
    public ModuleColumn() {
        super();
    }

    public String getModuleName(Job<?, ?> job) {
        ModuleAction action = job.getAction(ModuleAction.class);
        return (action != null) ? action.getModuleName() : null;
    }

    public String getModuleGraphUrl(Job<?, ?> job) {
        ModuleGraphJobAction action = job.getAction(ModuleGraphJobAction.class);
        return (action != null) ? action.getUrlName() : null;
    }

    @Extension(ordinal = DEFAULT_COLUMNS_ORDINAL_PROPERTIES_START - 3)
    @Symbol("module")
    public static final class DescriptorImpl extends ListViewColumnDescriptor {
        @Override
        public String getDisplayName() {
            return "Module";
        }

        @Override
        public boolean shownByDefault() {
            return false;
        }
    }
}

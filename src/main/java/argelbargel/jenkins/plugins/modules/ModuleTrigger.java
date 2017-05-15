package argelbargel.jenkins.plugins.modules;


import argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate;
import argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate.ActionsPredicateDescriptor;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.DependencyGraph;
import hudson.model.DependencyGraph.Dependency;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.DependencyDeclarer;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static argelbargel.jenkins.plugins.modules.ModuleUtils.allModules;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.allNames;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.buildUpstream;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.findProject;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.moduleExists;
import static java.util.Collections.singleton;


public final class ModuleTrigger extends Trigger<AbstractProject> implements DependencyDeclarer {
    private final ModuleAction action;

    @DataBoundConstructor
    public ModuleTrigger(String name) {
        super();
        this.action = new ModuleAction(name);
    }

    public String getName() {
        return action.getName();
    }

    public Collection<ModuleDependency> getDependencies() {
        return ModuleDependency.wrap(action.getDependencies());
    }

    @DataBoundSetter
    public void setDependencies(List<ModuleDependency> deps) {
        action.setDependencies(ModuleDependency.unwrap(deps));
    }

    public int getDependencyWaitInterval() {
        return (int) (action.getDependencyWaitInterval() / 1000);
    }

    @DataBoundSetter
    public void setDependencyWaitInterval(int interval) {
        action.setDependencyWaitInterval(interval * 1000);
    }

    public String getTriggerWhenResultBetterOrEqualTo() {
        return action.getTriggerResult().toString();
    }

    @DataBoundSetter
    public void setTriggerWhenResultBetterOrEqualTo(String result) {
        action.setTriggerResult(Result.fromString(result));
    }

    public List<ActionsPredicate> getPredicates() {
        return action.getPredicates();
    }


    @DataBoundSetter
    public void setPredicates(List<ActionsPredicate> predicates) {
        action.setPredicates(predicates);
    }

    @Override
    public void buildDependencyGraph(AbstractProject owner, DependencyGraph graph) {
        for (String dependency : action.getDependencies()) {
            ModuleAction module = ModuleAction.get(dependency);
            if (module != null) {
                graph.addDependency(new DependencyImpl(module.getProject(), owner));
            }
        }
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return singleton(action);
    }

    private static class DependencyImpl extends Dependency {
        DependencyImpl(AbstractProject upstream, AbstractProject downstream) {
            super(upstream, downstream);
        }

        @Override
        public boolean shouldTriggerBuild(AbstractBuild upstream, TaskListener listener, List<Action> actions) {
            return upstream.getParent() instanceof AbstractProject && shouldTriggerBuild(upstream.getProject(), upstream);
        }

        private boolean shouldTriggerBuild(AbstractProject<?, ?> project, Run<?, ?> run) {
            ModuleAction module = ModuleAction.get(project);
            return module != null && module.shouldTriggerDownstream(run.getResult());
        }
    }


    @Extension
    @Symbol("moduleTrigger")
    public static class DescriptorImpl extends TriggerDescriptor {
        @Override
        @Nonnull
        public String getDisplayName() {
            return "Job represents a module";
        }

        @Override
        public boolean isApplicable(Item item) {
            return item instanceof AbstractProject;
        }

        public List<ActionsPredicateDescriptor> getPredicateDescriptors() {
            return ActionsPredicateDescriptor.getAll();
        }

        @Restricted(NoExternalUse.class)
        public FormValidation doCheckName(@QueryParameter String name, @AncestorInPath AbstractProject context) {
            if (StringUtils.isBlank(name)) {
                return FormValidation.error("module name must not be blank");
            }

            if (moduleExists(name) && !context.equals(findProject(name))) {
                return FormValidation.error("a module with the name " + name + " already exists");
            }

            return FormValidation.ok();
        }

        @Restricted(NoExternalUse.class)
        public ComboBoxModel doFillNameItems(@QueryParameter String name) {
            Set<String> names = allNames();
            names.removeAll(buildUpstream(name));
            names.removeAll(allModules());

            return new ComboBoxModel(names);
        }

        @Restricted(NoExternalUse.class)
        public ListBoxModel doFillTriggerWhenResultBetterOrEqualToItems() {
            ListBoxModel model = new ListBoxModel();
            model.add(Result.SUCCESS.toString());
            model.add(Result.UNSTABLE.toString());
            model.add(Result.FAILURE.toString());
            return model;
        }
    }
}

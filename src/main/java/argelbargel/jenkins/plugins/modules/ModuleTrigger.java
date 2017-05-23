package argelbargel.jenkins.plugins.modules;


import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph.Dependency;
import argelbargel.jenkins.plugins.modules.graph.ModuleGraphJobAction;
import argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate;
import argelbargel.jenkins.plugins.modules.predicates.ActionsPredicate.ActionsPredicateDescriptor;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
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
import static java.util.Arrays.asList;


public final class ModuleTrigger extends Trigger<ParameterizedJob> {
    private final ModuleAction action;

    @DataBoundConstructor
    public ModuleTrigger(String name) {
        super();
        this.action = new ModuleAction(name);
    }

    public String getName() {
        return action.getModuleName();
    }

    @SuppressWarnings("unused") // used by config.jelly
    public Collection<ModuleDependency> getDependencies() {
        return ModuleDependency.wrap(action.getDependencies());
    }

    @DataBoundSetter
    public void setDependencies(List<ModuleDependency> deps) {
        action.setDependencies(ModuleDependency.unwrap(deps));
    }


    @SuppressWarnings("unused") // used by config.jelly
    public int getDependencyWaitInterval() {
        return (int) (action.getDependencyWaitInterval() / 1000);
    }

    @DataBoundSetter
    public void setDependencyWaitInterval(int interval) {
        action.setDependencyWaitInterval(interval * 1000);
    }

    @SuppressWarnings("unused") // used by config.jelly
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

    @DataBoundSetter
    public void setTriggerDownstreamWithCurrentParameters(boolean value) {
        action.setTriggerDownstreamWithCurrentParameters(value);
    }

    @SuppressWarnings("unused") // used by config.jelly
    public boolean getTriggerDownstreamWithCurrentParameters() {
        return action.getTriggerDownstreamWithCurrentParameters();
    }

    void buildDependencyGraph(Job<?, ?> owner, ModuleDependencyGraph graph) {
        for (String dependency : action.getDependencies()) {
            ModuleAction module = ModuleAction.get(dependency);
            if (module != null) {
                graph.addDependency(new DependencyImpl(module.getJob(), owner));
            }
        }
    }

    @Override
    public void start(ParameterizedJob project, boolean newInstance) {
        super.start(project, newInstance);
        ModuleDependencyGraph.rebuild();
    }

    @Override
    public void stop() {
        super.stop();
        ModuleDependencyGraph.rebuild();
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return asList(action, new ModuleGraphJobAction(getName()));
    }

    private static class DependencyImpl extends Dependency {
        DependencyImpl(Job<?, ?> upstream, Job<?, ?> downstream) {
            super(upstream, downstream);
        }

        @Override
        public boolean shouldTriggerBuild(Run<?, ?> build, TaskListener listener, List<Action> actions) {
            ModuleAction module = ModuleAction.get(getUpstreamJob());
            return module.shouldTriggerDownstream(build.getResult()) && willNotBlock();
        }

        @SuppressWarnings("unchecked")
        private boolean willNotBlock() {
            List<Job<?, ?>> downstream = ModuleDependencyGraph.get().getDownstream(getUpstreamJob());
            Set<Job<?, ?>> upstream = ModuleDependencyGraph.get().getTransitiveUpstream(getDownstreamJob());
            upstream.remove(getUpstreamJob());
            downstream.retainAll(upstream);
            return downstream.isEmpty();
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
            return item instanceof ParameterizedJob;
        }

        @SuppressWarnings("unused") // used by config.jelly
        public List<ActionsPredicateDescriptor> getPredicateDescriptors() {
            return ActionsPredicateDescriptor.getAll();
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // used by config.jelly
        public FormValidation doCheckName(@QueryParameter String name, @AncestorInPath Job context) {
            if (StringUtils.isBlank(name)) {
                return FormValidation.error("module name must not be blank");
            }

            if (moduleExists(name) && !context.equals(findProject(name))) {
                return FormValidation.error("a module with the name " + name + " already exists");
            }

            return FormValidation.ok();
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // used by config.jelly
        public ComboBoxModel doFillNameItems(@QueryParameter String name) {
            Set<String> names = allNames();
            names.removeAll(buildUpstream(name));
            names.removeAll(allModules());

            return new ComboBoxModel(names);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // used by config.jelly
        public ListBoxModel doFillTriggerWhenResultBetterOrEqualToItems() {
            ListBoxModel model = new ListBoxModel();
            model.add(Result.SUCCESS.toString());
            model.add(Result.UNSTABLE.toString());
            model.add(Result.FAILURE.toString());
            return model;
        }
    }
}

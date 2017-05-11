package argelbargel.jenkins.plugins.modules;


import argelbargel.jenkins.plugins.modules.predicates.AndQueueDataPredicate;
import argelbargel.jenkins.plugins.modules.predicates.QueueDataPredicate;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.DependencyGraph;
import hudson.model.DependencyGraph.Dependency;
import hudson.model.Item;
import hudson.model.Result;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static hudson.model.Result.SUCCESS;
import static hudson.model.Result.fromString;
import static java.util.Collections.emptyList;


public final class Module extends Trigger<AbstractProject> implements Dependable, DependencyDeclarer {
    private final String name;
    private Collection<ModuleDependency> dependencies;
    private int dependencyWaitInterval;
    private Result triggerWhenResultBetterOrEqualTo;
    private List<QueueDataPredicate> predicates;

    @DataBoundConstructor
    public Module(String name) {
        super();
        this.name = name;
        this.dependencies = emptyList();
        this.dependencyWaitInterval = 0;
        this.triggerWhenResultBetterOrEqualTo = SUCCESS;
        this.predicates = emptyList();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AbstractProject getProject() {
        return Registry.registry().project(name);
    }

    public Collection<ModuleDependency> getDependencies() {
        return dependencies;
    }

    @DataBoundSetter
    public void setDependencies(Collection<ModuleDependency> deps) {
        dependencies = deps;
    }

    public int getDependencyWaitInterval() {
        return dependencyWaitInterval;
    }

    @DataBoundSetter
    public void setDependencyWaitInterval(int interval) {
        dependencyWaitInterval = interval;
    }

    public String getTriggerWhenResultBetterOrEqualTo() {
        return triggerWhenResultBetterOrEqualTo.toString();
    }

    @DataBoundSetter
    public void setTriggerWhenResultBetterOrEqualTo(String result) {
        triggerWhenResultBetterOrEqualTo = fromString(result);
    }

    public List<QueueDataPredicate> getPredicates() {
        return predicates;
    }


    @DataBoundSetter
    public void setPredicates(List<QueueDataPredicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public void buildDependencyGraph(AbstractProject owner, DependencyGraph graph) {
        for (ModuleDependency dependency : getDependencies()) {
            AbstractProject project = dependency.getProject();
            if (project != null) {
                // TODO: alternativ eigene QueueAction, die die wartende Job berücksichtigt
                graph.addDependency(new DependencyImpl(project, owner));
            }
        }
    }

    @Override
    public void start(AbstractProject project, boolean newInstance) {
        Set<String> names = new HashSet<>(dependencies.size());
        for (Dependable dependency : dependencies) {
            String dep = dependency.getName();
            if (StringUtils.isNotBlank(dep) && !Registry.registry().upstream(dep).contains(name)) {
                names.add(dependency.getName());
            }
        }

        Registry.registry().register(name, project, names);
        super.start(project, newInstance);
    }

    @Override
    public void stop() {
        Registry.registry().unregister(name);
        super.stop();
    }

    QueueDataPredicate getPredicate() {
        return new AndQueueDataPredicate(getPredicates());
    }

    private class DependencyImpl extends Dependency {
        public DependencyImpl(AbstractProject upstream, AbstractProject downstream) {
            super(upstream, downstream);
        }

        @Override
        public boolean shouldTriggerBuild(AbstractBuild build, TaskListener listener, List<Action> actions) {
            return (build.getResult().isBetterOrEqualTo(triggerWhenResultBetterOrEqualTo));
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

        public List<QueueDataPredicate.QueuePredicateDescriptor> getPredicateDescriptors() {
            return QueueDataPredicate.QueuePredicateDescriptor.getAll();
        }

        @Restricted(NoExternalUse.class)
        public FormValidation doCheckName(@QueryParameter String name, @AncestorInPath AbstractProject context) {
            if (StringUtils.isBlank(name)) {
                return FormValidation.error("module name must not be blank");
            }

            if (Registry.registry().moduleExists(name) && !context.equals(Registry.registry().project(name))) {
                return FormValidation.error("a module with the name " + name + " already exists");
            }

            return FormValidation.ok();
        }

        @Restricted(NoExternalUse.class)
        public ComboBoxModel doFillNameItems(@QueryParameter String name) {
            Set<String> names = Registry.registry().names();
            names.removeAll(Registry.registry().upstream(name));
            names.removeAll(Registry.registry().modules());

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

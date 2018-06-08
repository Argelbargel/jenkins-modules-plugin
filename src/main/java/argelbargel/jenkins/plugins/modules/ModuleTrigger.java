package argelbargel.jenkins.plugins.modules;


import argelbargel.jenkins.plugins.modules.parameters.TriggerParameter;
import argelbargel.jenkins.plugins.modules.parameters.TriggerParameter.TriggerParameterDescriptor;
import argelbargel.jenkins.plugins.modules.queue.predicates.QueuePredicate;
import argelbargel.jenkins.plugins.modules.queue.predicates.QueuePredicate.QueuePredicateDescriptor;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static argelbargel.jenkins.plugins.modules.DescriptorUtils.getTriggerResultItems;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.allModuleNames;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.allModuleNamesWithJobs;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.buildUpstream;
import static argelbargel.jenkins.plugins.modules.ModuleUtils.findJobs;
import static argelbargel.jenkins.plugins.modules.queue.predicates.QueuePredicate.QueuePredicateDescriptor.getAllQueuePredicateDescriptors;
import static hudson.model.Result.SUCCESS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;


public final class ModuleTrigger extends Trigger<ParameterizedJob> {
    public static ModuleTrigger get(Job job) {
        return ParameterizedJobMixIn.getTrigger(job, ModuleTrigger.class);
    }

    private final ModuleAction action;
    private String triggerResult;
    private boolean triggerWithCurrentParameters;
    private List<TriggerParameter> triggerParameters;
    private List<TriggerParameter> downstreamParameters;


    @DataBoundConstructor
    public ModuleTrigger(String moduleName) {
        super();
        this.action = new ModuleAction(moduleName);
        this.triggerResult = SUCCESS.toString();
        this.triggerWithCurrentParameters = false;
        this.triggerParameters = emptyList();
        this.downstreamParameters = emptyList();
    }

    @SuppressWarnings("unused") // used by config.jelly
    public String getModuleName() {
        return action.getModuleName();
    }

    @SuppressWarnings("unused") // used by config.jelly
    public Collection<UpstreamDependency> getUpstreamDependencies() {
        return action.getUpstreamDependencies();
    }

    @DataBoundSetter
    public void setUpstreamDependencies(List<UpstreamDependency> deps) {
        action.setUpstreamDependencies(deps);
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
    public String getTriggerResult() {
        return triggerResult;
    }

    @DataBoundSetter
    public void setTriggerResult(String result) {
        triggerResult = result;
    }

    public List<QueuePredicate> getQueuePredicates() {
        return action.getQueuePredicates();
    }

    @DataBoundSetter
    public void setQueuePredicates(List<QueuePredicate> predicates) {
        action.setQueuePredicates(predicates);
    }

    @DataBoundSetter
    public void setTriggerWithCurrentParameters(boolean value) {
        triggerWithCurrentParameters = value;
    }

    @SuppressWarnings("unused") // used by config.jelly
    public boolean getTriggerWithCurrentParameters() {
        return triggerWithCurrentParameters;
    }

    @DataBoundSetter
    public void setTriggerParameters(List<TriggerParameter> parameters) {
        triggerParameters = parameters;
    }

    @SuppressWarnings("unused") // used by config.jelly
    public List<TriggerParameter> getTriggerParameters() {
        return triggerParameters;
    }

    @SuppressWarnings("unused") // used by config.jelly
    public List<TriggerParameter> getDownstreamParameters() {
        return downstreamParameters;
    }

    @DataBoundSetter
    public void setDownstreamParameters(List<TriggerParameter> parameters) {
        downstreamParameters = parameters;
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
        return singletonList(action);
    }

    @Restricted(NoExternalUse.class)
    public boolean mustCancelDownstream(Result result) {
        return result.isWorseThan(Result.fromString(triggerResult));
    }

    @Restricted(NoExternalUse.class)
    public boolean shouldTriggerDownstream(Run<?, ?> upstream) {
        return shouldTriggerDownstream(upstream.getResult(), upstream.getAction(ParametersAction.class));
    }

    private boolean shouldTriggerDownstream(Result result, ParametersAction parameters) {
        return result.isBetterOrEqualTo(Result.fromString(triggerResult)) && shouldTriggerDownstream(parameters);
    }

    private boolean shouldTriggerDownstream(ParametersAction parameters) {
        for (TriggerParameter t : triggerParameters) {
            if (!t.test(parameters)) {
                return false;
            }
        }

        return true;
    }

    void addUpstreamDependencies(ModuleDependencyGraph graph, Job<?, ?> owner) {
        action.addUpstreamDependencies(graph, owner);
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
        public List<QueuePredicateDescriptor> getQueuePredicateDescriptors() {
            return getAllQueuePredicateDescriptors();
        }

        @SuppressWarnings("unused") // used by config.jelly
        public List<TriggerParameterDescriptor> getParameterDescriptors() {
            return TriggerParameterDescriptor.getAll();
        }

        @SuppressWarnings("unused") // used by config.jelly
        public String getJobName() {
            return Stapler.getCurrentRequest().findAncestorObject(Job.class).getFullName();
        }

        @SuppressWarnings("unused") // used by config.jelly
        public int getDependencyWaitInterval() {
            return getDefaults().getDependencyWaitInterval();
        }

        @SuppressWarnings("unused") // used by config.jelly
        public String getTriggerResult() {
            return getDefaults().getTriggerResult();
        }

        @SuppressWarnings("unused") // used by config.jelly
        public boolean getTriggerWithCurrentParameters() {
            return getDefaults().getTriggerWithCurrentParameters();
        }

        private ModuleTriggerDefaults getDefaults() {
            return Jenkins.get().getDescriptorByType(ModuleTriggerDefaults.class);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // used by config.jelly
        public FormValidation doCheckModuleName(@QueryParameter String moduleName, @AncestorInPath Job context) {
            if (StringUtils.isBlank(moduleName)) {
                return FormValidation.error("module name must not be blank");
            }

            Optional<Job<?, ?>> duplicate = findJobs(moduleName).stream()
                    .filter(j -> !isSameItemOrHasSameGroup(context, j))
                    .findAny();

            if (duplicate.isPresent()) {
                return FormValidation.error("a module with the name " + moduleName + " already exists at " + duplicate.get().getParent().getFullDisplayName());
            }

            return FormValidation.ok();
        }

        private boolean isSameItemOrHasSameGroup(Item a, Item b) {
            if (a.equals(b)) {
                return true;
            }

            if (a.getParent().equals(b.getParent())) {
                return !a.getParent().equals(Jenkins.get().getItemGroup());
            }

            return false;
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // used by config.jelly
        public ComboBoxModel doFillModuleNameItems(@QueryParameter String moduleName) {
            Set<String> names = allModuleNames();
            names.removeAll(buildUpstream(moduleName));
            names.removeAll(allModuleNamesWithJobs());

            return new ComboBoxModel(names);
        }

        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // used by config.jelly
        public ListBoxModel doFillTriggerResultItems() {
            return getTriggerResultItems();
        }
    }
}

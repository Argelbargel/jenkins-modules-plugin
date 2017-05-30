package argelbargel.jenkins.plugins.modules.parameters;


import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.util.ComboBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public abstract class TriggerParameter extends AbstractDescribableImpl<TriggerParameter> implements Serializable, ExtensionPoint {
    private final String name;

    TriggerParameter(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public final boolean test(ParametersAction action) {
        if (action == null) {
            return false;
        }

        for (ParameterValue value : action.getParameters()) {
            if (test(value)) {
                return true;
            }
        }

        return false;
    }

    private boolean test(ParameterValue value) {
        return value.getName().equals(name) && test(value.getValue());
    }

    protected abstract boolean test(Object value);


    public static abstract class TriggerParameterDescriptor extends Descriptor<TriggerParameter> {
        private final Collection<Class<? extends ParameterDefinition>> parameterClasses;

        @SuppressWarnings("WeakerAccess") // part of public api
        protected TriggerParameterDescriptor(Collection<Class<? extends ParameterDefinition>> parameterClasses) {
            this.parameterClasses = parameterClasses;
        }

        public static List<TriggerParameterDescriptor> getAll() {
            return Jenkins.getInstance().getDescriptorList(TriggerParameter.class);
        }

        @Restricted(NoExternalUse.class)
        public final ComboBoxModel doFillNameItems(@AncestorInPath Job<?, ?> job) {
            ComboBoxModel model = new ComboBoxModel();
            ParametersDefinitionProperty property = job.getProperty(ParametersDefinitionProperty.class);
            if (property != null) {
                for (ParameterDefinition parameter : filterParameters(property.getParameterDefinitions())) {
                    model.add(parameter.getName());
                }
            }

            return model;
        }

        private Collection<ParameterDefinition> filterParameters(Collection<ParameterDefinition> parameters) {
            Collection<ParameterDefinition> filtered = new ArrayList<>();
            for (ParameterDefinition p : parameters) {
                for (Class<? extends ParameterDefinition> c : parameterClasses) {
                    if (c.isInstance(p)) {
                        filtered.add(p);
                    }
                }
            }

            return filtered;
        }
    }
}

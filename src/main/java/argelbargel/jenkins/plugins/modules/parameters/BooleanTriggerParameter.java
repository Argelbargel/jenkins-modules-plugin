package argelbargel.jenkins.plugins.modules.parameters;


import hudson.Extension;
import hudson.model.BooleanParameterDefinition;
import hudson.model.ParameterDefinition;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collections;


@SuppressWarnings("unused") // extension
public class BooleanTriggerParameter extends TriggerParameter {
    private final Boolean expected;

    @DataBoundConstructor
    public BooleanTriggerParameter(String name, Boolean expected) {
        super(name);
        this.expected = expected;
    }

    @SuppressWarnings("WeakerAccess") // used in config.jelly
    public Boolean getExpected() {
        return expected;
    }

    @Override
    protected boolean test(Object value) {
        return value != null ? Boolean.valueOf(value.toString()).equals(expected) : !expected;
    }

    @Override
    public String toString() {
        return getName() + "=" + getExpected();
    }


    @Extension
    public static class DescriptorImpl extends TriggerParameterDescriptor {
        public DescriptorImpl() {
            super(Collections.<Class<? extends ParameterDefinition>>singleton(BooleanParameterDefinition.class));
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Boolean Parameter";
        }
    }
}

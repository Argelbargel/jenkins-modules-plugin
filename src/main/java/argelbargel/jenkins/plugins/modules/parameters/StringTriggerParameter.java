package argelbargel.jenkins.plugins.modules.parameters;


import hudson.Extension;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.ParameterDefinition;
import hudson.model.StringParameterDefinition;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Arrays;


@SuppressWarnings("unused") // extension
public class StringTriggerParameter extends TriggerParameter {
    private final String expected;

    @DataBoundConstructor
    public StringTriggerParameter(String name, String expected) {
        super(name);
        this.expected = expected;
    }

    @SuppressWarnings("WeakerAccess") // used in config.jelly
    public String getExpected() {
        return expected;
    }

    @Override
    protected boolean test(Object value) {
        return value != null ? value.toString().equals(expected) : StringUtils.isEmpty(expected);
    }

    @Override
    public String toString() {
        return getName() + "=" + getExpected();
    }

    @Extension
    public static class DescriptorImpl extends TriggerParameterDescriptor {
        public DescriptorImpl() {
            super(Arrays.<Class<? extends ParameterDefinition>>asList(StringParameterDefinition.class, ChoiceParameterDefinition.class));
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Text Parameter";
        }
    }

}

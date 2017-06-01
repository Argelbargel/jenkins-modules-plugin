package argelbargel.jenkins.plugins.modules.parameters;


import hudson.Extension;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.StringParameterDefinition;
import hudson.model.TextParameterValue;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Arrays;


@SuppressWarnings("unused") // extension
public final class TextTriggerParameter extends TriggerParameter {
    private final String expected;

    @DataBoundConstructor
    public TextTriggerParameter(String name, String expected) {
        super(name);
        this.expected = expected;
    }

    @SuppressWarnings("WeakerAccess") // used in config.jelly
    public String getExpected() {
        return expected;
    }

    @Override
    public ParameterValue createValue() {
        return new TextParameterValue(getName(), expected);
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
    @Symbol("textTriggerParameter")
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

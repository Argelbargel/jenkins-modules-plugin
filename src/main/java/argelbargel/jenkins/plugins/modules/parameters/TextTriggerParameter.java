package argelbargel.jenkins.plugins.modules.parameters;


import com.thoughtworks.xstream.converters.UnmarshallingContext;
import hudson.Extension;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.StringParameterDefinition;
import hudson.model.TextParameterValue;
import hudson.util.XStream2;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Arrays;


@SuppressWarnings("unused") // extension
public final class TextTriggerParameter extends TriggerParameter {
    private static final long serialVersionUID = 1L;

    private String value;

    @Deprecated // >= 0.9.2
    @SuppressWarnings({"DeprecatedIsStillUsed", "unused"})
    private transient String expected;


    @DataBoundConstructor
    public TextTriggerParameter(String name, String value) {
        super(name);
        this.value = value;
    }

    @SuppressWarnings("WeakerAccess") // used in config.jelly
    public String getValue() {
        return value;
    }

    @Override
    public ParameterValue createParameterValue() {
        return new TextParameterValue(getName(), value);
    }

    @Override
    protected boolean test(Object value) {
        return value != null ? value.toString().equals(this.value) : StringUtils.isEmpty(this.value);
    }


    @Override
    public String toString() {
        return getName() + "=" + getValue();
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


    @Deprecated // >= 0.9.2
    @SuppressWarnings({"deprecation", "unused"})
    public static final class ConverterImpl extends XStream2.PassthruConverter<TextTriggerParameter> {
        public ConverterImpl(XStream2 xstream) {
            super(xstream);
        }

        @Override
        protected void callback(TextTriggerParameter obj, UnmarshallingContext context) {
            if (obj.value == null) {
                obj.value = obj.expected;
            }
        }
    }
}

package argelbargel.jenkins.plugins.modules.parameters;


import com.thoughtworks.xstream.converters.UnmarshallingContext;
import hudson.Extension;
import hudson.model.BooleanParameterDefinition;
import hudson.model.BooleanParameterValue;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.util.XStream2;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collections;


@SuppressWarnings("unused") // extension
public final class BooleanTriggerParameter extends TriggerParameter {
    private static final long serialVersionUID = 1L;

    private Boolean value;

    @Deprecated // >= 0.9.2
    @SuppressWarnings({"DeprecatedIsStillUsed", "unused"})
    private transient Boolean expected;


    @DataBoundConstructor
    public BooleanTriggerParameter(String name, Boolean value) {
        super(name);
        this.value = value;
    }

    @SuppressWarnings("WeakerAccess") // used in config.jelly
    public Boolean getValue() {
        return value;
    }

    @Override
    public ParameterValue createParameterValue() {
        return new BooleanParameterValue(getName(), value);
    }

    @Override
    protected boolean test(Object value) {
        return value != null ? Boolean.valueOf(value.toString()).equals(this.value) : !this.value;
    }

    @Override
    public String toString() {
        return getName() + "=" + getValue();
    }


    @Extension
    @Symbol("booleanTriggerParameter")
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


    @Deprecated // >= 0.9.2
    @SuppressWarnings({"deprecation", "unused"})
    public static final class ConverterImpl extends XStream2.PassthruConverter<BooleanTriggerParameter> {
        public ConverterImpl(XStream2 xstream) {
            super(xstream);
        }

        @Override
        protected void callback(BooleanTriggerParameter obj, UnmarshallingContext context) {
            if (obj.value == null) {
                obj.value = obj.expected;
            }
        }
    }
}

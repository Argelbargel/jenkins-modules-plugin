package argelbargel.jenkins.plugins.modules;


import com.thoughtworks.xstream.converters.UnmarshallingContext;
import hudson.Extension;
import hudson.util.ListBoxModel;
import hudson.util.XStream2;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;

import static argelbargel.jenkins.plugins.modules.DescriptorUtils.getTriggerResultItems;


@Extension
public final class ModuleTriggerDefaults extends GlobalConfiguration {
    private static final String PROPERTY_DEPENDENCY_WAIT_INTERVAL = "dependencyWaitInterval";
    private static final String PROPERTY_TRIGGER_RESULT = "triggerResult";
    private static final String PROPERTY_TRIGGER_WITH_CURRENT_PARAMETERS = "triggerWithCurrentParameters";

    private int waitInterval;
    private String triggerResult;
    private boolean triggerWithCurrentParameters;

    @Deprecated // >= 0.9.1
    @SuppressWarnings({"DeprecatedIsStillUsed", "unused"})
    private transient Boolean triggerDownstreamWithCurrentParameters;


    public ModuleTriggerDefaults() {
        super();
        load();
    }

    @SuppressWarnings({"unused", "WeakerAccess"}) // used by config.jelly
    public int getDependencyWaitInterval() {
        return waitInterval;
    }

    @SuppressWarnings({"unused", "WeakerAccess"}) // used by config.jelly
    public String getTriggerResult() {
        return triggerResult;
    }

    @SuppressWarnings({"unused", "WeakerAccess"}) // used by config.jelly
    public boolean getTriggerWithCurrentParameters() {
        return triggerWithCurrentParameters;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        waitInterval = json.getInt(PROPERTY_DEPENDENCY_WAIT_INTERVAL);
        triggerResult = json.getString(PROPERTY_TRIGGER_RESULT);
        triggerWithCurrentParameters = json.getBoolean(PROPERTY_TRIGGER_WITH_CURRENT_PARAMETERS);
        save();
        return super.configure(req, json);
    }

    @Restricted(NoExternalUse.class)
    @SuppressWarnings("unused") // used by config.jelly
    public ListBoxModel doFillTriggerResult() {
        return getTriggerResultItems();
    }


    @Deprecated // >= 0.9.1
    @SuppressWarnings({"deprecation", "unused"})
    public static final class ConverterImpl extends XStream2.PassthruConverter<ModuleTriggerDefaults> {
        public ConverterImpl(XStream2 xstream) {
            super(xstream);
        }

        @Override
        protected void callback(ModuleTriggerDefaults obj, UnmarshallingContext context) {
            if (obj.triggerDownstreamWithCurrentParameters != null) {
                obj.triggerWithCurrentParameters = obj.triggerDownstreamWithCurrentParameters;
            }
        }
    }
}

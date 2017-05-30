package argelbargel.jenkins.plugins.modules;


import hudson.Extension;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;

import static argelbargel.jenkins.plugins.modules.DescriptorUtils.getTriggerWhenResultBetterOrEqualToItems;


@Extension
public final class ModuleTriggerDefaults extends GlobalConfiguration {
    private static final String PROPERTY_DEPENDENCY_WAIT_INTERVAL = "dependencyWaitInterval";
    private static final String PROPERTY_TRIGGER_WHEN_RESULT_BETTER_OR_EQUAL_TO = "triggerWhenResultBetterOrEqualTo";
    private static final String PROPERTY_TRIGGER_DOWNSTREAM_WITH_CURRENT_PARAMETERS = "triggerDownstreamWithCurrentParameters";

    private int waitInterval;
    private String triggerResult;
    private boolean triggerDownstreamWithCurrentParameters;

    public ModuleTriggerDefaults() {
        super();
        load();
    }

    @SuppressWarnings({"unused", "WeakerAccess"}) // used by config.jelly
    public int getDependencyWaitInterval() {
        return waitInterval;
    }

    @SuppressWarnings({"unused", "WeakerAccess"}) // used by config.jelly
    public String getTriggerWhenResultBetterOrEqualTo() {
        return triggerResult;
    }

    @SuppressWarnings({"unused", "WeakerAccess"}) // used by config.jelly
    public boolean getTriggerDownstreamWithCurrentParameters() {
        return triggerDownstreamWithCurrentParameters;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        waitInterval = json.getInt(PROPERTY_DEPENDENCY_WAIT_INTERVAL);
        triggerResult = json.getString(PROPERTY_TRIGGER_WHEN_RESULT_BETTER_OR_EQUAL_TO);
        triggerDownstreamWithCurrentParameters = json.getBoolean(PROPERTY_TRIGGER_DOWNSTREAM_WITH_CURRENT_PARAMETERS);
        save();
        return super.configure(req, json);
    }

    @Restricted(NoExternalUse.class)
    @SuppressWarnings("unused") // used by config.jelly
    public ListBoxModel doFillTriggerWhenResultBetterOrEqualToItems() {
        return getTriggerWhenResultBetterOrEqualToItems();
    }
}

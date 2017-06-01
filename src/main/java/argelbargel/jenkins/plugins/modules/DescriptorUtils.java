package argelbargel.jenkins.plugins.modules;


import hudson.model.Result;
import hudson.util.ListBoxModel;


final class DescriptorUtils {
    static ListBoxModel getTriggerResultItems() {
        ListBoxModel model = new ListBoxModel();
        model.add(Result.SUCCESS.toString());
        model.add(Result.UNSTABLE.toString());
        model.add(Result.FAILURE.toString());
        return model;
    }

    private DescriptorUtils() { /* no instances required */ }
}

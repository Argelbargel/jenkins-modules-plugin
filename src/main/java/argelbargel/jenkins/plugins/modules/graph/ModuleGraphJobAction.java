package argelbargel.jenkins.plugins.modules.graph;


import argelbargel.jenkins.plugins.modules.ModuleAction;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;


public class ModuleGraphJobAction implements Action {
    private final String module;

    public ModuleGraphJobAction(String module) {
        this.module = module;
    }

    @Override
    public String getIconFileName() {
        return ModuleGraph.ICON_FILE_NAME;
    }

    @Override
    public String getDisplayName() {
        return getJob() != null ? ModuleGraph.DISPLAY_NAME : null;
    }

    @Override
    public String getUrlName() {
        Job job = getJob();
        if (job == null) {
            return null;
        }
        Run<?, ?> run = job.getLastBuild();
        return run != null ? Jenkins.getInstance().getRootUrl() + run.getUrl() + ModuleGraph.URL_NAME : null;
    }

    private Job getJob() {
        ModuleAction moduleAction = ModuleAction.get(module);
        return moduleAction != null ? moduleAction.getJob() : null;
    }
}
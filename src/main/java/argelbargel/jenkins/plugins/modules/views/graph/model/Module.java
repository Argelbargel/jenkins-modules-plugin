package argelbargel.jenkins.plugins.modules.views.graph.model;


import hudson.model.Job;

import java.util.List;

import static argelbargel.jenkins.plugins.modules.ModuleAction.getModuleAction;
import static argelbargel.jenkins.plugins.modules.views.graph.model.Status.UNKNOWN;
import static java.util.Collections.emptyList;


public final class Module extends Node<Job> {
    public Module(GraphType type, Job job, boolean current, int index) {
        super(type, job, index, current);
    }

    @Override
    public String getTitle() {
        return getModuleAction(payload()).getModuleName();
    }

    @Override
    public String getUrl() {
        return payload().getUrl();
    }

    @Override
    public String getDescription() {
        return payload().getDescription();
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isBuilding() {
        return false;
    }

    @Override
    public Status getStatus() {
        return UNKNOWN;
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public String getStartTime() {
        return "";
    }

    @Override
    public String getDuration() {
        return "";
    }

    @Override
    public String getTimestamp() {
        return "";
    }

    @Override
    public List<String> getParameters() {
        return emptyList();
    }
}

package argelbargel.jenkins.plugins.modules.views.graph;


import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph;
import argelbargel.jenkins.plugins.modules.views.graph.model.GraphType;
import argelbargel.jenkins.plugins.modules.views.graph.model.Module;
import argelbargel.jenkins.plugins.modules.views.graph.model.Node;
import hudson.model.Action;
import hudson.model.Job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static argelbargel.jenkins.plugins.modules.views.graph.model.GraphType.MODULE;


public final class ModuleJobGraph extends AbstractModuleGraph<Job> implements Action {
    private static final String URL_NAME = "moduleJobGraph";
    private static final String DISPLAY_NAME = "Module Graph";
    private static final String ICON_FILE_NAME = "/plugin/modules-plugin/images/24x24/icon-module-graph.png";

    ModuleJobGraph(Job job) {
        super(MODULE, job);
    }

    @Override
    public String getIconFileName() {
        return ICON_FILE_NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }

    @Override
    protected Node<Job> createNode(GraphType type, Job job, int index, boolean current) {
        return new Module(type, job, current, index);
    }

    @Override
    protected Collection<Job> getRoots() {
        return ModuleDependencyGraph.get().getRoots(getPayload());
    }

    @Override
    protected List<Job> getDownstream(Job current, Job target) throws ExecutionException, InterruptedException {
        ModuleDependencyGraph dependencyGraph = ModuleDependencyGraph.get();
        List<Job> downstream = new ArrayList<>();
        for (Job job : dependencyGraph.getDownstream(current)) {
            if (isRelevant(job, target) && !dependencyGraph.hasIndirectDependencies(current, job)) {
                downstream.add(job);
            }
        }

        return downstream;
    }
}

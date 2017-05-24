package argelbargel.jenkins.plugins.modules.graph;


import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph;
import argelbargel.jenkins.plugins.modules.graph.model.GraphType;
import argelbargel.jenkins.plugins.modules.graph.model.Module;
import argelbargel.jenkins.plugins.modules.graph.model.Node;
import hudson.model.Action;
import hudson.model.Job;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static argelbargel.jenkins.plugins.modules.graph.model.GraphType.MODULE;


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
    protected Node<Job> createNode(GraphType type, Job job, int index) {
        return new Module(type, job, index);
    }

    @Override
    protected Set<Job> getRoots() {
        return ModuleDependencyGraph.get().getRoots(getPayload());
    }

    @Override
    protected List<Job> getDownstream(Job payload) throws ExecutionException, InterruptedException {
        List<Job> downstream = new ArrayList<>();
        ModuleDependencyGraph dependencyGraph = ModuleDependencyGraph.get();
        for (Job job : dependencyGraph.getDownstream(payload)) {
            if (!dependencyGraph.hasIndirectDependencies(payload, job)) {
                downstream.add(job);
            }
        }

        return downstream;
    }
}

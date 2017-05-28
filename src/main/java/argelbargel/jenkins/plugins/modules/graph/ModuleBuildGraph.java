package argelbargel.jenkins.plugins.modules.graph;


import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph;
import argelbargel.jenkins.plugins.modules.graph.model.Build;
import argelbargel.jenkins.plugins.modules.graph.model.GraphType;
import argelbargel.jenkins.plugins.modules.graph.model.Node;
import argelbargel.jenkins.plugins.modules.queue.ModuleBlockedAction;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static argelbargel.jenkins.plugins.modules.graph.model.GraphType.BUILD;


/**
 * Compute the graph of related builds, based on {@link Cause.UpstreamCause}.
 */
public class ModuleBuildGraph extends AbstractModuleGraph<Run> implements Action {
    private static final String URL_NAME = "moduleBuildGraph";
    private static final String DISPLAY_NAME = "Build Graph";
    private static final String ICON_FILE_NAME = "/plugin/modules-plugin/images/24x24/icon-build-graph.png";

    ModuleBuildGraph(Run run) {
        super(BUILD, run);
    }

    public String getIconFileName() {
        return ICON_FILE_NAME;
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public String getUrlName() {
        return URL_NAME;
    }

    @Override
    protected Node<Run> createNode(GraphType type, Run payload, int index, boolean current) {
        return new Build(type, payload, current, ++index);
    }

    @Override
    protected Collection<Run> getRoots() {
        Run run = getPayload();
        Set<Run> roots = new HashSet<>();
        findRoots(roots, run);
        return roots;
    }

    private static void findRoots(Set<Run> roots, Run<?, ?> run) {
        if (run != null) {
            boolean isRoot = true;
            for (Cause cause : run.getCauses()) {
                if (cause instanceof Cause.UpstreamCause) {
                    isRoot = false;
                    findRoots(roots, ((Cause.UpstreamCause) cause).getUpstreamRun());
                }
            }

            if (isRoot) {
                roots.add(run);
            }
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    protected List<Run> getDownstream(Run payload, Run target) throws ExecutionException, InterruptedException {
        List<Run> runs = new ArrayList<>();

        Job parent = payload.getParent();
        ModuleDependencyGraph dependencyGraph = ModuleDependencyGraph.get();
        for (Job downstream : dependencyGraph.getDownstream(parent)) {
            if (isRelevant(downstream, target.getParent()) && !dependencyGraph.hasIndirectDependencies(parent, downstream)) {
                addTriggeredAndBlockedBuilds(runs, (List<Run<?, ?>>) downstream.getBuilds(), payload);
            }
        }

        return runs;
    }

    private void addTriggeredAndBlockedBuilds(List<Run> runs, List<Run<?, ?>> downstream, Run run) {
        for (Run<?, ?> d : downstream) {
            if (wasTriggeredBy(d, run) || hasRunFormerlyBlockedBy(d, run)) {
                runs.add(d);
            }
        }
    }

    private boolean hasRunFormerlyBlockedBy(Run<?, ?> downstream, Run run) {
        ModuleBlockedAction blocked = ModuleBlockedAction.get(downstream);
        return blocked != null && blocked.wasBlockedBy(run);
    }

    private boolean wasTriggeredBy(Run<?, ?> downstream, Run run) {
        for (Cause cause : downstream.getCauses()) {
            if (cause instanceof Cause.UpstreamCause && wasTriggeredBy((Cause.UpstreamCause) cause, run)) {
                return true;
            }
        }
        return false;
    }

    private static boolean wasTriggeredBy(Cause.UpstreamCause cause, Run run) {
        return cause.getUpstreamProject().equals(run.getParent().getFullName()) && cause.getUpstreamBuild() == run.getNumber();
    }
}

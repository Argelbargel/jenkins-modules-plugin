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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static argelbargel.jenkins.plugins.modules.graph.model.GraphType.BUILD;


/**
 * Compute the graph of related builds, based on {@link Cause.UpstreamCause}.
 */
public class ModuleGraph extends AbstractModuleGraph<Run> implements Action {
    static final String URL_NAME = "moduleGraph";
    static final String DISPLAY_NAME = "Module Graph";
    static final String ICON_FILE_NAME = "/plugin/modules-plugin/images/16x16/chain.png";

    public ModuleGraph(Run run) {
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
    protected Node<Run> createNode(GraphType type, Run payload, int index) {
        return new Build(type, payload, ++index);
    }

    @Override
    protected Set<Run> getRoots() {
        Run run = getPayload();
        Set<Run> roots = new HashSet<>();
        if (run != null) {
            findRoots(roots, run);
        }
        return roots;
    }

    private static void findRoots(Set<Run> roots, Run<?, ?> run) {
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


    @SuppressWarnings("unchecked")
    @Override
    protected List<Run> getDownstream(Node<Run> b) throws ExecutionException, InterruptedException {
        Run<?, ?> run = b.payload();
        List<Run> runs = new ArrayList<>();
        for (Job downstream : ModuleDependencyGraph.get().getDownstream(run.getParent())) {
            addTriggeredAndBlockedBuilds(runs, (List<Run<?, ?>>) downstream.getBuilds(), run);
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

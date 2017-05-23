package argelbargel.jenkins.plugins.modules.graph;


import argelbargel.jenkins.plugins.modules.graph.model.Build;
import argelbargel.jenkins.plugins.modules.graph.model.Column;
import argelbargel.jenkins.plugins.modules.graph.model.Connector;
import argelbargel.jenkins.plugins.modules.graph.model.Graph;
import argelbargel.jenkins.plugins.modules.graph.model.Node;
import com.google.gson.GsonBuilder;
import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Cause;
import hudson.model.Run;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static argelbargel.jenkins.plugins.modules.graph.ModuleDependencyDeclarer.findRoots;
import static argelbargel.jenkins.plugins.modules.graph.ModuleDependencyDeclarer.getDownstream;


/**
 * Compute the graph of related builds, based on {@link Cause.UpstreamCause}.
 */
@ExportedBean
public class ModuleGraph implements Action {
    static final String URL_NAME = "moduleGraph";
    static final String DISPLAY_NAME = "Module Graph";
    static final String ICON_FILE_NAME = "/plugin/modules-plugin/images/16x16/chain.png";

    private final Run run;
    private DirectedGraph<Build, Edge> graph;
    private transient int index = 0;

    public ModuleGraph(Run<?, ?> run) {
        this.run = run;
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

    public Api getApi() {
        return new Api(this);
    }

    public Run getRun() {
        return run;
    }

    private DirectedGraph<Build, Edge> computeGraph() throws ExecutionException, InterruptedException, ClassNotFoundException, IOException {
        if (graph == null) {
            index = 0;
            for (Run run : findRoots(run)) {
                Build build = new Build(run, ++index);
                graph = new SimpleDirectedGraph<>(Edge.class);
                graph.addVertex(build);
                computeGraphFrom(build);
                setupDisplayGrid(build);
            }
        }
        return graph;
    }

    private void computeGraphFrom(Build b) throws ExecutionException, InterruptedException, IOException {
        List<Run> runs = getDownstream(b.payload());
        for (Run run : runs) {
            if (run != null) {
                Build next = getExecution(run);
                graph.addVertex(next);
                graph.addEdge(b, next, new Edge(b, next));
                computeGraphFrom(next);
            }
        }
    }

    private Build getExecution(Run r) {
        for (Build build : graph.vertexSet()) {
            if (build.payload().equals(r)) {
                return build;
            }
        }
        return new Build(r, ++index);
    }

    /**
     * Assigns a unique row and column to each build in the graph
     */
    private void setupDisplayGrid(Build build) {
        List<List<Build>> allPaths = findAllPaths(build);
        // make the longer paths bubble up to the top
        Collections.sort(allPaths, new Comparator<List<Build>>() {
            public int compare(List<Build> runs1, List<Build> runs2) {
                return runs2.size() - runs1.size();
            }
        });
        // set the build row and column of each build
        // loop backwards through the rows so that the lowest path a job is on
        // will be assigned
        for (int row = allPaths.size() - 1; row >= 0; row--) {
            List<Build> path = allPaths.get(row);
            for (int column = 0; column < path.size(); column++) {
                Build job = path.get(column);
                job.setColumn(Math.max(job.getColumn(), column));
                job.setRow(row + 1);
            }
        }
    }

    /**
     * Finds all paths that start at the given vertex
     *
     * @param start the origin
     * @return a list of paths
     */
    private List<List<Build>> findAllPaths(Build start) {
        List<List<Build>> allPaths = new LinkedList<>();
        if (graph.outDegreeOf(start) == 0) {
            // base case
            List<Build> singlePath = new LinkedList<>();
            singlePath.add(start);
            allPaths.add(singlePath);
        } else {
            for (Edge edge : graph.outgoingEdgesOf(start)) {
                List<List<Build>> allPathsFromTarget = findAllPaths(edge.getTarget());
                for (List<Build> path : allPathsFromTarget) {
                    path.add(0, start);
                }
                allPaths.addAll(allPathsFromTarget);
            }
        }
        return allPaths;
    }

    @Exported
    @SuppressWarnings("unused") // used by index.jelly
    public String getModuleGraph() throws InterruptedException, ExecutionException, ClassNotFoundException, IOException {
        DirectedGraph<Build, Edge> iGraph = this.computeGraph();
        Graph graph = new Graph();
        ArrayList<Node> nodeArrayList = new ArrayList<>();
        ArrayList<Connector> buildGraphConnectorsModelArrayList = new ArrayList<>();
        for (Build item : iGraph.vertexSet()) {
            graph.setBuilding(graph.getBuilding() | item.isBuilding());
            item.setBuildClass(item.payload() == run ? "currentBuild" : "");
            nodeArrayList.add(item);
        }


        ArrayList<Column> columnArrayList = new ArrayList<>();
        for (Node node : nodeArrayList) {
            if (node.getColumn() >= columnArrayList.size()) {
                Column column = new Column();
                ArrayList<Node> nodes = new ArrayList<>();
                nodes.add(node);
                column.setNodes(nodes);
                columnArrayList.add(column);
            } else {
                Column column = columnArrayList.get(node.getColumn());
                column.getNodes().add(node);
            }
        }

        for (Edge edge : iGraph.edgeSet()) {
            Connector connector = new Connector();
            connector.setSource(edge.getSource().getId());
            connector.setTarget(edge.getTarget().getId());
            buildGraphConnectorsModelArrayList.add(connector);
        }
        graph.setNodesSize(nodeArrayList.size());
        graph.setNodes(columnArrayList);
        graph.setConnectors(buildGraphConnectorsModelArrayList);
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Node.class, new NodeSerializer());
        return builder.create().toJson(graph);
    }

    private static class Edge implements Serializable {
        private final Build source;
        private final Build target;

        Edge(Build source, Build target) {
            this.source = source;
            this.target = target;
        }

        public Build getSource() {
            return source;
        }

        public Build getTarget() {
            return target;
        }

        @Override
        public String toString() {
            return source.toString() + " -> " + target.toString();
        }
    }
}

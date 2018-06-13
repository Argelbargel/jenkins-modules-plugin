package argelbargel.jenkins.plugins.modules.views.graph;


import argelbargel.jenkins.plugins.modules.ModuleDependencyGraph;
import argelbargel.jenkins.plugins.modules.views.graph.model.Graph;
import argelbargel.jenkins.plugins.modules.views.graph.model.GraphType;
import argelbargel.jenkins.plugins.modules.views.graph.model.Node;
import com.google.gson.GsonBuilder;
import hudson.model.Api;
import hudson.model.Job;
import jenkins.model.Jenkins;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


@ExportedBean
public abstract class AbstractModuleGraph<PAYLOAD> {
    private final GraphType type;
    private final PAYLOAD payload;
    private transient DirectedGraph<Node<PAYLOAD>, Edge> graph;
    private transient int index = 0;
    private transient Map<PAYLOAD, Node<PAYLOAD>> nodes;

    AbstractModuleGraph(GraphType type, PAYLOAD payload) {
        this.type = type;
        this.payload = payload;
    }

    @SuppressWarnings({"WeakerAccess", "unused"}) // used by index.jelly
    public final PAYLOAD getPayload() {
        return payload;
    }

    public final Api getApi() {
        return new Api(this);
    }

    @Exported
    @SuppressWarnings("unused") // used by api/json
    public final String getRootUrl() {
        return Jenkins.get().getRootUrl();
    }

    @Exported
    @SuppressWarnings("unused") // used by index.jelly
    public final String getGraph() throws InterruptedException, ExecutionException {
        DirectedGraph<Node<PAYLOAD>, Edge> iGraph = this.computeGraph();

        Graph graph = new Graph(type);

        for (Node node : iGraph.vertexSet()) {
            graph.addNode(node);
            graph.setBuilding(graph.getBuilding() | node.isBuilding());
        }

        for (Edge edge : iGraph.edgeSet()) {
            graph.addConnector(edge.getSource().getId(), edge.getTarget().getId());
        }

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Node.class, new NodeSerializer());
        return builder.create().toJson(graph);
    }

    final boolean isRelevant(Job job, Job target) {
        return job.equals(target) || ModuleDependencyGraph.get().hasDependency(job, target);
    }

    abstract Node<PAYLOAD> createNode(GraphType type, PAYLOAD payload, int index, boolean current);

    abstract Collection<PAYLOAD> getRoots();

    abstract List<PAYLOAD> getDownstream(PAYLOAD payload, PAYLOAD target) throws ExecutionException, InterruptedException;

    private DirectedGraph<Node<PAYLOAD>, Edge> computeGraph() throws ExecutionException, InterruptedException {
        if (graph == null) {
            graph = new SimpleDirectedGraph<>(new EdgeFactory());
            nodes = new HashMap<>();
            index = 0;
            for (PAYLOAD root : getRoots()) {
                Node<PAYLOAD> node = getOrCreateNode(root);
                graph.addVertex(node);
                computeGraphFrom(node);
                setupDisplayGrid(node);
            }
        }
        return graph;
    }

    private Node<PAYLOAD> getOrCreateNode(PAYLOAD payload) {
        if (!nodes.containsKey(payload)) {
            nodes.put(payload, createNode(type, payload, ++index, this.payload.equals(payload)));
        }

        return nodes.get(payload);
    }

    private void computeGraphFrom(Node<PAYLOAD> node) throws ExecutionException, InterruptedException {
        for (PAYLOAD downstream : getDownstream(node.payload(), payload)) {
            Node<PAYLOAD> next = getOrCreateNode(downstream);
            graph.addVertex(next);
            graph.addEdge(node, next, new Edge(node, next));
            computeGraphFrom(next);
        }
    }

    /**
     * Assigns a unique row and column to each node in the graph
     */
    private void setupDisplayGrid(Node<PAYLOAD> node) {
        List<List<Node>> allPaths = findAllPaths(node);
        // make the longer paths bubble up to the top
        allPaths.sort((Comparator<List>) (paths1, paths2) -> paths2.size() - paths1.size());
        // set the node row and column of each node
        // loop backwards through the rows so that the lowest path a job is on
        // will be assigned
        for (int row = allPaths.size() - 1; row >= 0; row--) {
            List<Node> path = allPaths.get(row);
            for (int column = 0; column < path.size(); column++) {
                Node job = path.get(column);
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
    private List<List<Node>> findAllPaths(Node<PAYLOAD> start) {
        List<List<Node>> allPaths = new LinkedList<>();
        if (graph.outDegreeOf(start) == 0) {
            // base case
            List<Node> singlePath = new LinkedList<>();
            singlePath.add(start);
            allPaths.add(singlePath);
        } else {
            for (Edge edge : graph.outgoingEdgesOf(start)) {
                List<List<Node>> allPathsFromTarget = findAllPaths(edge.getTarget());
                for (List<Node> path : allPathsFromTarget) {
                    path.add(0, start);
                }
                allPaths.addAll(allPathsFromTarget);
            }
        }
        return allPaths;
    }


    private class EdgeFactory implements org.jgrapht.EdgeFactory<Node<PAYLOAD>, Edge> {
        @Override
        public Edge createEdge(Node<PAYLOAD> source, Node<PAYLOAD> target) {
            return new Edge(source, target);
        }
    }


    private class Edge implements Serializable {
        private final Node<PAYLOAD> source;
        private final Node<PAYLOAD> target;

        Edge(Node<PAYLOAD> source, Node<PAYLOAD> target) {
            this.source = source;
            this.target = target;
        }

        public Node<PAYLOAD> getSource() {
            return source;
        }

        public Node<PAYLOAD> getTarget() {
            return target;
        }

        @Override
        public String toString() {
            return source.toString() + " -> " + target.toString();
        }
    }
}

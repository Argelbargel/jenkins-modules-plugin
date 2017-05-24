package argelbargel.jenkins.plugins.modules.graph;


import argelbargel.jenkins.plugins.modules.graph.model.Column;
import argelbargel.jenkins.plugins.modules.graph.model.Connector;
import argelbargel.jenkins.plugins.modules.graph.model.Graph;
import argelbargel.jenkins.plugins.modules.graph.model.GraphType;
import argelbargel.jenkins.plugins.modules.graph.model.Node;
import com.google.gson.GsonBuilder;
import hudson.model.Api;
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
import java.util.Set;
import java.util.concurrent.ExecutionException;


@ExportedBean
public abstract class AbstractModuleGraph<PAYLOAD> {
    private final GraphType type;
    private final PAYLOAD payload;
    private DirectedGraph<Node<PAYLOAD>, Edge> graph;
    private transient int index = 0;

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
    @SuppressWarnings("unused") // used by index.jelly
    public final String getGraph() throws InterruptedException, ExecutionException, ClassNotFoundException, IOException {
        DirectedGraph<Node<PAYLOAD>, Edge> iGraph = this.computeGraph();
        Graph graph = new Graph(type);
        ArrayList<Node> nodeArrayList = new ArrayList<>();
        ArrayList<Connector> buildGraphConnectorsModelArrayList = new ArrayList<>();
        for (Node item : iGraph.vertexSet()) {
            graph.setBuilding(graph.getBuilding() | item.isBuilding());
            item.setCurrentNode(item.payload() == payload);
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


    protected abstract Node<PAYLOAD> createNode(GraphType type, PAYLOAD payload, int index);

    protected abstract Set<PAYLOAD> getRoots();

    protected abstract List<PAYLOAD> getDownstream(PAYLOAD payload) throws ExecutionException, InterruptedException;


    private DirectedGraph<Node<PAYLOAD>, Edge> computeGraph() throws ExecutionException, InterruptedException, ClassNotFoundException, IOException {
        if (graph == null) {
            index = 0;
            for (PAYLOAD run : getRoots()) {
                Node<PAYLOAD> build = createNode(run);
                graph = new SimpleDirectedGraph<>(new EdgeFactory());
                graph.addVertex(build);
                computeGraphFrom(build);
                setupDisplayGrid(build);
            }
        }
        return graph;
    }

    private Node<PAYLOAD> createNode(PAYLOAD run) {
        return createNode(type, run, ++index);
    }

    private void computeGraphFrom(Node<PAYLOAD> node) throws ExecutionException, InterruptedException, IOException {
        for (PAYLOAD downstream : getDownstream(node.payload())) {
            if (downstream != null) {
                Node<PAYLOAD> next = getNode(downstream);
                graph.addVertex(next);
                graph.addEdge(node, next, new Edge(node, next));
                computeGraphFrom(next);
            }
        }
    }

    private Node<PAYLOAD> getNode(PAYLOAD r) {
        for (Node<PAYLOAD> build : graph.vertexSet()) {
            if (build.payload().equals(r)) {
                return build;
            }
        }
        return createNode(r);
    }

    /**
     * Assigns a unique row and column to each build in the graph
     */
    private void setupDisplayGrid(Node<PAYLOAD> build) {
        List<List<Node>> allPaths = findAllPaths(build);
        // make the longer paths bubble up to the top
        Collections.sort(allPaths, new Comparator<List>() {
            public int compare(List runs1, List runs2) {
                return runs2.size() - runs1.size();
            }
        });
        // set the build row and column of each build
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

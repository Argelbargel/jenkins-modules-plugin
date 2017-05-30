package argelbargel.jenkins.plugins.modules.views.graph.model;


import java.util.ArrayList;


public class Column {
    private final ArrayList<Node> nodes;

    Column() {
        nodes = new ArrayList<>();
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    void addNode(Node node) {
        nodes.add(node);
    }
}

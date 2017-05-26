package argelbargel.jenkins.plugins.modules.graph.model;


import java.util.ArrayList;


public class Graph {
    private final GraphType type;
    private final ArrayList<Column> columns;
    private final ArrayList<Connector> connectors;
    private Boolean isBuilding = false;

    public Graph(GraphType type) {
        this.type = type;
        this.columns = new ArrayList<>();
        this.connectors = new ArrayList<>();
    }

    public GraphType getType() {
        return type;
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }

    public void addNode(Node node) {
        while (node.getColumn() >= columns.size()) {
            columns.add(new Column());
        }

        columns.get(node.getColumn()).addNode(node);
    }

    public void addConnector(String source, String target) {
        connectors.add(new Connector(source, target));
    }

    public ArrayList<Connector> getConnectors() {
        return connectors;
    }

    public Boolean getBuilding() {
        return isBuilding;
    }

    public void setBuilding(Boolean building) {
        isBuilding = building;
    }
}

package argelbargel.jenkins.plugins.modules.graph.model;


import java.util.ArrayList;


public class Graph {

    private ArrayList<Column> nodes;
    private ArrayList<Connector> connectors;
    private Boolean isBuilding = false;
    private Integer nodesSize;

    public ArrayList<Column> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Column> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<Connector> getConnectors() {
        return connectors;
    }

    public void setConnectors(ArrayList<Connector> connectors) {
        this.connectors = connectors;
    }

    public Boolean getBuilding() {
        return isBuilding;
    }

    public void setBuilding(Boolean building) {
        isBuilding = building;
    }

    public Integer getNodesSize() {
        return nodesSize;
    }

    public void setNodesSize(Integer nodesSize) {
        this.nodesSize = nodesSize;
    }
}

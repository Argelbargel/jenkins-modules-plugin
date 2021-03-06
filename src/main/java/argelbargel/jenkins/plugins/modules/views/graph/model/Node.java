package argelbargel.jenkins.plugins.modules.views.graph.model;


import java.io.Serializable;
import java.util.List;


public abstract class Node<PAYLOAD> implements Serializable {
    private final GraphType type;
    private final PAYLOAD payload;
    private final int index;
    private final boolean currentNode;
    private int column;
    private int row;

    Node(GraphType type, PAYLOAD payload, int index, boolean current) {
        this.type = type;
        this.payload = payload;
        this.index = index;
        this.currentNode = current;
    }

    public final PAYLOAD payload() {
        return payload;
    }

    public final GraphType getType() {
        return type;
    }

    public final String getId() {
        return type.name().toLowerCase() + "-" + index;
    }

    public final int getColumn() {
        return column;
    }

    public final int getRow() {
        return row;
    }

    @Override
    public final String toString() {
        return getId() + ": " + getTitle();
    }

    public final boolean isCurrentNode() {
        return currentNode;
    }

    public final void setColumn(int column) {
        this.column = column;
    }

    public final void setRow(int row) {
        this.row = row;
    }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof Node && payload().equals(((Node) obj).payload());
    }

    @Override
    public final int hashCode() {
        return payload().hashCode();
    }

    public abstract String getTitle();

    public abstract String getUrl();

    public abstract String getDescription();

    public abstract Status getStatus();

    public abstract boolean isStarted();

    public abstract boolean isBuilding();

    public abstract int getProgress();

    public abstract String getStartTime();

    public abstract String getDuration();

    public abstract String getTimestamp();

    public abstract List<String> getParameters();
}

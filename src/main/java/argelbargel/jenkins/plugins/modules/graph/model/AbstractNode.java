package argelbargel.jenkins.plugins.modules.graph.model;


import jenkins.model.Jenkins;


abstract class AbstractNode implements Node {
    private final Type type;
    private final int index;
    private int column;
    private int row;
    private String buildClass = "";

    AbstractNode(Type type, int index) {
        this.type = type;
        this.index = index;
    }

    @Override
    public final Type getType() {
        return type;
    }

    @Override
    public final String getRootUrl() {
        return Jenkins.getInstance().getRootUrl();
    }

    @Override
    public final String getId() {
        return type.name().toLowerCase() + "-" + index;
    }

    @Override
    public final int getColumn() {
        return column;
    }

    @Override
    public final int getRow() {
        return row;
    }

    @Override
    public final String toString() {
        return getId() + ": " + getTitle();
    }

    @Override
    public final String getBuildClass() {
        return buildClass;
    }

    public final void setBuildClass(String buildClass) {
        this.buildClass = buildClass;
    }

    public final void setColumn(int column) {
        this.column = column;
    }

    public final void setRow(int row) {
        this.row = row;
    }
}

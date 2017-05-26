package argelbargel.jenkins.plugins.modules.graph.model;


public class Connector {
    private final String source;
    private final String target;

    Connector(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }
}

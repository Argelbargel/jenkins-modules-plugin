package argelbargel.jenkins.plugins.modules.queue;


import java.io.Serializable;


public final class Blocker implements Serializable {
    private final String name;
    private final Integer build;
    private final String url;

    Blocker(String name, Integer build, String url) {
        this.name = name;
        this.build = build;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public Integer getBuild() {
        return build;
    }

    @SuppressWarnings("unused")// used by summary.jelly
    public String getUrl() {
        return url;
    }
}

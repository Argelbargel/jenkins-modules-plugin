package argelbargel.jenkins.plugins.modules.queue;


import java.io.Serializable;


public final class Blocker implements Serializable {
    private final String moduleName;
    private final String fullName;
    private final Integer build;
    private final String url;

    Blocker(String moduleName, String fullName, Integer build, String url) {
        this.moduleName = moduleName;
        this.fullName = fullName;
        this.build = build;
        this.url = url;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getFullName() {
        return fullName;
    }

    public Integer getBuild() {
        return build;
    }

    @SuppressWarnings("unused")// used by summary.jelly
    public String getUrl() {
        return url;
    }
}

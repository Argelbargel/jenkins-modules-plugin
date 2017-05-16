package argelbargel.jenkins.plugins.modules.queue;


import java.io.Serializable;

import static java.lang.System.currentTimeMillis;


public final class Blocker implements Serializable {
    private final long id;
    private final String name;
    private final Integer build;
    private final String url;
    private final long start;
    private long end;

    Blocker(long id, String name, Integer build, String url) {
        this.id = id;
        this.name = name;
        this.build = build;
        this.url = url;
        this.start = currentTimeMillis();
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

    @SuppressWarnings("WeakerAccess") // used by summary.jelly
    public long getDuration() {
        return end - start;
    }

    long id() {
        return id;
    }

    long unblock() {
        end = currentTimeMillis();
        return getDuration();
    }
}

package argelbargel.jenkins.plugins.modules.queue;


import hudson.Util;

import java.io.Serializable;

import static java.lang.System.currentTimeMillis;


public final class Blocker implements Serializable {
    private final long id;
    private final String name;
    private final Integer build;
    private final String url;
    private final long start;
    private Long end;

    Blocker(long id, String name, Integer build, String url) {
        this.id = id;
        this.name = name;
        this.build = build;
        this.url = url;
        this.start = currentTimeMillis();
        this.end = null;
    }

    public String getName() {
        return name;
    }

    public Integer getBuild() {
        return build;
    }

    @SuppressWarnings("unused")// used by summary.jelly
    public String getUrl() {
        return build != null ? url + build : url;
    }

    @SuppressWarnings("WeakerAccess") // used by summary.jelly
    public String getDuration() {
        return Util.getTimeSpanString(end != null ? end - start : currentTimeMillis() - start);
    }

    boolean isBlocked() {
        return end == null;
    }

    long id() {
        return id;
    }

    long unblock() {
        end = currentTimeMillis();
        return end - start;
    }
}

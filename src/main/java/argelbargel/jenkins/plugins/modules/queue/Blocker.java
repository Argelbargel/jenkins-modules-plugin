package argelbargel.jenkins.plugins.modules.queue;


import hudson.model.Job;
import hudson.model.Queue.Item;
import hudson.model.Queue.LeftItem;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.io.Serializable;


public final class Blocker implements Serializable {
    private final long queueId;
    private final String moduleName;
    private final String fullName;
    private final Integer build;
    private final String url;

    Blocker(long id, String moduleName, String fullName, Integer build, String url) {
        this.queueId = id;
        this.moduleName = moduleName;
        this.fullName = fullName;
        this.build = build;
        this.url = url;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getFullDisplayName() {
        Job job = Jenkins.get().getItemByFullName(fullName, Job.class);
        return job != null ? job.getFullDisplayName() : null;
    }

    public Integer getBuild() {
        return build;
    }

    public String getProjectName() {
        Job job = Jenkins.get().getItemByFullName(fullName, Job.class);
        return job != null ? job.getName() : null;
    }

    public String getProjectFullName() {
        Job job = Jenkins.get().getItemByFullName(fullName, Job.class);
        return job != null ? job.getFullName() : null;
    }

    public Run getRun() {
        if (build == null) {
            return null;
        }
        Job job = Jenkins.get().getItemByFullName(fullName, Job.class);
        return job != null ? job.getBuildByNumber(build) : null;
    }

    @SuppressWarnings("unused")// used by summary.jelly
    public String getUrl() {
        return url;
    }

    boolean isBlocking() {
        if (build != null) {
            return isBlocking(Jenkins.get().getItemByFullName(fullName, Job.class));
        } else {
            return isBlocking(Jenkins.get().getQueue().getItem(queueId));
        }
    }

    private boolean isBlocking(Item item) {
        return !LeftItem.class.isInstance(item);
    }

    private boolean isBlocking(Job job) {
        return job != null && isBlocking(job.getBuildByNumber(build));
    }

    private boolean isBlocking(Run build) {
        return build != null && build.isBuilding();
    }
}

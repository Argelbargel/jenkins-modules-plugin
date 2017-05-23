package argelbargel.jenkins.plugins.modules.graph.model;


import java.io.Serializable;
import java.util.List;


public interface Node extends Serializable {
    enum Type {
        JOB,
        BUILD
    }

    Type getType();

    String getId();

    int getRow();

    int getColumn();

    String getTitle();

    String getColor();

    String getUrl();

    String getDescription();

    boolean isStarted();

    boolean isBuilding();

    String getStatus();

    int getProgress();

    String getStartTime();

    String getDuration();

    String getRootUrl();

    String getTimestamp();

    List<String> getParameters();

    String getBuildClass();
}

package argelbargel.jenkins.plugins.modules.graph.model;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.BallColor;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;


/**
 * A wrapper on a Run that maintains additional layout information, used during graphical rendering.
 *
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class Build implements Serializable {
    private static final long serialVersionUID = 1;

    private transient Run build;

    // A unique number that identifies when in the FlowAbstractBuild this job was started
    private final int buildIndex;

    private String id;

    private String buildUrl;

    private String startTime;

    private String buildName;

    private String buildNumber;

    private BallColor iconColor;

    private String fullDisplayName;

    private String description;

    private boolean building;

    private String durationString;

    private String buildSummaryStatusString;

    private int displayColumn;

    private int displayRow;

    private List<String> parameters = null;

    public Build(Run build, int buildIndex) {
        this.build = build;
        this.buildIndex = buildIndex;
        this.id = "build-" + buildIndex;
        this.buildUrl = this.getBuildFromUtil().getAbsoluteUrl();
        this.startTime = isStarted() ? DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT).format(build.getTime())
                : "";

        this.buildName = this.getBuildFromUtil().getParent().getName();

        this.buildNumber = "" + this.getBuildFromUtil().number;
        this.iconColor = this.getBuildFromUtil().getIconColor();
        this.fullDisplayName = this.getBuildFromUtil().getFullDisplayName();
        this.description = this.getBuildFromUtil().getDescription();
        this.building = this.getBuildFromUtil().isBuilding();
        this.durationString = this.getBuildFromUtil().getDurationString();
        this.buildSummaryStatusString = this.getBuildFromUtil().getBuildStatusSummary().message;

        // If this build has parameters then get useful Strings out to be displayed in the graph.
        ParametersAction parameters = this.getBuildFromUtil().getAction(ParametersAction.class);
        if (parameters != null) {
            this.parameters = new LinkedList<>();
            for (ParameterValue p : parameters.getParameters()) {
                if (p != null) {
                    // The String and Boolean parameters are the most useful to display and they 
                    // are prefixed by "(Type) ", so chop that off, and add the rest in.
                    String paramString = p.toString();
                    if (paramString.startsWith("(") && paramString.contains(" ")) {
                        this.parameters.add(paramString.substring(paramString.indexOf(" ") + 1));
                    } else {
                        this.parameters.add(paramString);
                    }
                }
            }
        }
    }

    public Build(int buildIndex) {
        this.buildIndex = buildIndex;
    }

    public BallColor getIconColor() {
        return iconColor;
    }

    public String getFullDisplayName() {
        return fullDisplayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean getBuilding() {
        return building;
    }

    public String getDurationString() {
        return durationString;
    }

    public String getBuildSummaryStatusString() {
        return buildSummaryStatusString;
    }

    public String getId() {
        return id;
    }

    public String getBuildUrl() {
        return buildUrl;
    }

    public String getStartTime() {
        if (build == null) {
            return startTime;
        }
        if (isStarted()) {
            return startTime = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT,
                    DateFormat.SHORT)
                    .format(build.getTime());
        }
        return "";
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public boolean isStarted() {
        return build == null || build.getTime() != null;
    }

    public Run<?, ?> getBuild() {
        return build;
    }

    public int getBuildIndex() {
        return buildIndex;
    }

    public int getDisplayColumn() {
        return displayColumn;
    }

    public int getDisplayRow() {
        return displayRow;
    }

    public void setDisplayColumn(int displayColumn) {
        this.displayColumn = displayColumn;
    }

    public void setDisplayRow(int displayRow) {
        this.displayRow = displayRow;
    }

    public String toString() {
        return (buildName != null && buildNumber != null ? buildName + " #" + buildNumber : "");
    }

    private Run getBuildFromUtil() {
        if (this.build == null) {
            throw new NullPointerException("Build has not been started.");
        }
        return this.build;
    }

    public List<String> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Build && build.equals(((Build) obj).build);
    }

    @Override
    public int hashCode() {
        if (build != null) {
            return build.hashCode();
        }
        return super.hashCode();
    }
}
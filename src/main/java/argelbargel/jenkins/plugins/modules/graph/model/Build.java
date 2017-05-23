package argelbargel.jenkins.plugins.modules.graph.model;


import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;

import static argelbargel.jenkins.plugins.modules.graph.model.Node.Type.BUILD;
import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;


/**
 * A wrapper on a Run that maintains additional layout information, used during graphical rendering.
 *
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class Build extends AbstractNode {
    private final Run build;

    public Build(Run build, int index) {
        super(BUILD, index);
        this.build = build;
    }

    public String getColor() {
        return build.getIconColor().getHtmlBaseColor();
    }

    public String getTitle() {
        return build.getFullDisplayName();
    }

    public String getDescription() {
        return build.getDescription();
    }

    public boolean isBuilding() {
        return build.isBuilding();
    }

    public String getDuration() {
        return build.getDurationString();
    }

    public String getTimestamp() {
        return isBuilding() ? build().getTimestampString() : "";
    }

    public int getProgress() {
        if (!isBuilding()) {
            return 0;
        }

        int progress = (int) round(100.0d * (currentTimeMillis() - build().getTimestamp().getTimeInMillis()) / build().getEstimatedDuration());
        return progress <= 100 ? progress : 99;
    }

    public String getStatus() {
        return build.getBuildStatusSummary().message;
    }

    @SuppressWarnings("deprecation")
    public String getUrl() {
        return build.getAbsoluteUrl();
    }

    public String getStartTime() {
        if (isStarted()) {
            return DateFormat.getDateTimeInstance(
                    DateFormat.SHORT,
                    DateFormat.SHORT)
                    .format(build.getTime());
        }

        return "";
    }

    @Override
    public boolean isStarted() {
        return !build.hasntStartedYet();
    }

    public Run<?, ?> build() {
        return build;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new LinkedList<>();
        ParametersAction action = build.getAction(ParametersAction.class);
        if (action != null) {
            for (ParameterValue p : action.getParameters()) {
                if (p != null) {
                    // The String and Boolean parameters are the most useful to display and they
                    // are prefixed by "(Type) ", so chop that off, and add the rest in.
                    String paramString = p.toString();
                    if (paramString.startsWith("(") && paramString.contains(" ")) {
                        parameters.add(paramString.substring(paramString.indexOf(" ") + 1));
                    } else {
                        parameters.add(paramString);
                    }
                }
            }
        }

        return parameters;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Build && build.equals(((Build) obj).build);
    }

    @Override
    public int hashCode() {
        return build.hashCode();
    }
}
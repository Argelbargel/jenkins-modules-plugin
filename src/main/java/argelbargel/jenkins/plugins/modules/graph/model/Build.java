package argelbargel.jenkins.plugins.modules.graph.model;


import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;


/**
 * A wrapper on a Run that maintains additional layout information, used during graphical rendering.
 *
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class Build extends Node<Run> {
    public Build(GraphType type, Run build, int index) {
        super(type, build, index);
    }

    public String getColor() {
        return payload().getIconColor().getHtmlBaseColor();
    }

    public String getTitle() {
        return payload().getFullDisplayName();
    }

    public String getDescription() {
        return payload().getDescription();
    }

    public boolean isBuilding() {
        return payload().isBuilding();
    }

    public String getDuration() {
        return payload().getDurationString();
    }

    public String getTimestamp() {
        return isBuilding() ? payload().getTimestampString() : "";
    }

    public int getProgress() {
        if (!isBuilding()) {
            return 0;
        }

        int progress = (int) round(100.0d * (currentTimeMillis() - payload().getTimestamp().getTimeInMillis()) / payload().getEstimatedDuration());
        return progress <= 100 ? progress : 99;
    }

    public String getStatus() {
        return payload().getBuildStatusSummary().message;
    }

    @SuppressWarnings("deprecation")
    public String getUrl() { // TODO!
        return payload().getAbsoluteUrl();
    }

    public String getStartTime() {
        if (isStarted()) {
            return DateFormat.getDateTimeInstance(
                    DateFormat.SHORT,
                    DateFormat.SHORT)
                    .format(payload().getTime());
        }

        return "";
    }

    @Override
    public boolean isStarted() {
        return !payload().hasntStartedYet();
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new LinkedList<>();
        ParametersAction action = payload().getAction(ParametersAction.class);
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
}
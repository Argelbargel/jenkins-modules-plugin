package argelbargel.jenkins.plugins.modules.views.graph.model;


import hudson.model.BallColor;


public final class Status {
    static final Status UNKNOWN = new Status(BallColor.GREY, "");

    private final String summary;
    private final String color;
    private final String image;

    Status(BallColor color, String summary) {
        this.color = color.getHtmlBaseColor();
        this.image = color.getImage();
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public String getColor() {
        return color;
    }

    public String getImage() {
        return image;
    }
}

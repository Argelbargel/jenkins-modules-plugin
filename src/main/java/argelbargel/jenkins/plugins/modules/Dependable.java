package argelbargel.jenkins.plugins.modules;


import hudson.model.AbstractProject;


interface Dependable {
    String getName();

    AbstractProject getProject();
}

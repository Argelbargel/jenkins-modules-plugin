# jenkins-modules-plugin [![Build Status](https://travis-ci.org/Argelbargel/jenkins-modules-plugin.svg?branch=master)](https://travis-ci.org/Argelbargel/jenkins-modules-plugin)

## Introduction

This plugin is similar to a combination of the [Build Blocker Plugin](https://plugins.jenkins.io/build-blocker-plugin),
[Parameterized Trigger Plugin](https://plugins.jenkins.io/parameterized-trigger) and perhaps the [Block queued job plugin](https://wiki.jenkins-ci.org/display/JENKINS/Block+queued+job+plugin).
It allows you to specify relationships between jobs and blocks them while downstream jobs are running or triggers them when upstream jobs
are completed. Additionally it adds a graphical view of those relationships, similar to what the [Build Graph View plugin](https://plugins.jenkins.io/buildgraph-view) does.

### So why? What problem does this plugin solve that the others do not?

First and foremost, this plugin uses its own mechanism to track relationships between jobs which is independent of the 
job type. This means you can declare relationships for any job (like pipeline jobs ([WorkflowJob](http://javadoc.jenkins.io/plugin/workflow-job/org/jenkinsci/plugins/workflow/job/WorkflowJob.html)s) for example),
not just old-style-projects ([AbstractProject](http://javadoc.jenkins-ci.org/hudson/model/AbstractProject.html)s).
Second, it uses its own namespace for the relationships, so you do not have to rely on the name of the job but can use 
any naming-scheme that suits your build-system (e.g. maven/gradle artifact ids).
Third, it allows you to define conditional relationships; this means downstream-projects are only blocked if both the 
upstream- and downstream-projects share some attribute (e.g. they were started by the same user, triggered by the same
upstream-project etc.).

### Okay, but why "modules"? Basically it's a plugin to build pipelines of jobs, isn't it?

Yes, this plugin is about building pipelines which span/consist of jenkins jobs. Unfortunally all the appropriate terms
like pipeline, stage etc. have their specific meanings since the advent of the [Pipeline Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Plugin).
Additionally, most older plugins containing the term "pipeline" (e.g. the [Build Pipeline Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Build+Pipeline+Plugin)) 
rely on Jenkins' builtin dependency-tracking mechanism which is restricted to old-style projects and will not work for
jobs using the new pipeline jobs. Thus i've tried to find a term which expresses that this plugin spans jobs 
(which by themself use a pipeline-script consisting of stages which might run on jenkins nodes) and is independent of
the known mechanism tracking project-dependencies. "Modules" is the best i came up with as a non-native english speaker.
If you have suggestions for a better term, i'll appreciate it.

### An example usecase, please?

Let's say you've got a project with the following components:

![
@startuml
skinparam handwritten true
component common
component model
component controllers
component views
common -- model
common -- controllers
common -- views
model -- controllers
model -- views
controllers -- views
@enduml](http://www.plantuml.com/plantuml/png/NOn13e0W30Jllg8Vu554caY4rfGgtvSW8dBhpkuqxPuEApR2PibbyQf8e7BYid8yc90KoXMP1X3POVWDI8L3G4a3lJpcceiCndnMovFLw6FLxJpgyFtSgmvl)

Each component is built by its own job in Jenkins. Whenever there is a change to the component containing the component
containing your common utilities or api you'll want to build and test the other components to see that your changes work
for all of them. Currently you can model the downstream-depedencies using the [Pipeline Build Step](https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Build+Step+Plugin) or
or a [Parameterized Trigger](https://plugins.jenkins.io/parameterized-trigger) but this way the downstream projects will
always be triggered if any upstream-project is completed. In this case the views-component is built three times; the 
first time when common is completed, the second time and third time when the model and the controller projects are
completed. This plugin recognizes the dependencies and doesn't trigger the build until the controller-component finishes
building. 
Additionally, when you make a change to both the common and the views-component at the same time, it blocks the build of
the views-component until all it's upstream-dependencies have completed their builds.
When there are many developers you can configure the plugin so that it recognizes which changes originate from the same
developer so that the pipeline are run in parallel for each set of changes.

## Screencast of plugin usage
![Screencast](./docs/screencast.gif)

## Credits
This plugin uses code from the [Build Graph View plugin](https://plugins.jenkins.io/buildgraph-view) and an adapted 
version of the [DependencyGraph](http://javadoc.jenkins-ci.org/hudson/model/DependencyGraph.html)-implementation from Jenkins itself. 
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
any naming-scheme that suits your build-system (e.g. gradle-project-names, maven-artifact-ids).
Third, it allows you the define conditional relationships; this means downstream-projects are only blocked if both the 
upstream- and downstream-projects share some attribute (e.g., they were started by the same user, triggered by the same
upstream-project).
 
### I still don't get it. What made you built it?

Let's say you've got your run of the mill MVC-project but your models, views and controllers are separated into different
(gradle or maven) projects and just depend on the artifacts built by these projects. Each of these projects has their
 own pipeline-job in Jenkins. Whenever there is a change to the
 model-layer you'll want to build and test your view and controller-layers, too. Currently you can model the downstream-
 depedencies using the [Pipeline Build Step](https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Build+Step+Plugin) but
 this step will always run. What if you've got a fourth dependency, let's say a project for a client that uses your 
 application which depends on both the controller and your views (okay, the MVC-example breaks here, but so what)? 
 In this case, this project will be triggered whenever one of it's upstream projects is completed not just once after 
 both projects are. Another example: let's say you've made a change to the model and to the view-layer. In this case
 you'll want the build of the view-layer to wait until the build of the model-layer has completed and incorporate the
 changes made there. That is what this plugins enables you to do. 
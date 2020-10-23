This plugin adds two new parameter types to job configuration - node and
label, this allows to dynamically select the node where a job/project
should be executed.

## Description

The plugin allows to configure additional parameters for a job. These
new parameter types are 'Node' and 'Label'. This is specially useful if
you want to execute the job on different nodes without changing the
configuration over and over again. It also allows you to use Jenkins in
a scenario where you would like to setup different nodes with the same
script/jobs configured - e.g. SW provisioning.  
Another usage scenario would be to configure a node maintenance job
which you could trigger on request on each node.

![](https://wiki.jenkins.io/download/attachments/57183146/selectParameter.jpg?version=1&modificationDate=1308076539000&api=v2)

Restrict where this project can be run

If your using a node or label parameter to run your job on a particular
node, you should not use the option "Restrict where this project can be
run" in the job configuration - it will not have any effect to the
selection of your node anymore!

-   #### Node

    Define a list of nodes on which the job should be allowed to be
    executed on. A default node used for scheduled jobs can be
    defined.  
    You are able to configure the job to run one after the other or even
    concurrent.

![](https://wiki.jenkins.io/download/attachments/57183146/config_plugin.jpg?version=1&modificationDate=1316361239000&api=v2)  
In case multi node selection was disabled, you get a dropdown to select
one node to execute the job on.

![](https://wiki.jenkins.io/download/attachments/57183146/triggerWithNode.jpg?version=1&modificationDate=1308076539000&api=v2)

If multi node selection was enabled, you get the chance to select
multiple nodes to run the job on. The job will then be executed on each
of the nodes, one after the other or concurrent - depending on the
configuration.

![](https://wiki.jenkins.io/download/attachments/57183146/multinode_selection.jpg?version=1&modificationDate=1315766641000&api=v2)

-   #### Label

    Define a label of 'Restrict where this project can be run' on the
    fly.

![](https://wiki.jenkins.io/download/attachments/57183146/labelParameter.jpg?version=1&modificationDate=1308076539000&api=v2)

![](https://wiki.jenkins.io/download/attachments/57183146/triggerWithLabel.jpg?version=1&modificationDate=1308076539000&api=v2)

#### Trigger via script

One can also trigger a job via remote call (e.g. script)

Trigger job on multiple nodes:

``` syntaxhighlighter-pre
curl --silent -u USER:PASSWORD --show-error --data 'json={"parameter":[{"name":"PARAMNAME","value":["node1","node2"]}]}&Submit=Build' http://localhost:8080/job/remote/build?token=SECTOKEN
```

Although the first format also supports passing just one node name as
parameter in the list, the plugin also supports to pas a simple
key/value parameter to trigger the job on  single node only:

``` syntaxhighlighter-pre
curl --silent -u USER:PASSWORD --show-error --data 'json={"parameter":[{"name":"PARAMNAME","value":"master"}]}&Submit=Build' http://localhost:8080/job/remote/build?token=SECTOKEN
```

If you have a 'label' parameter (instead of a 'node' parameter), then
the request should look like this:

``` syntaxhighlighter-pre
curl --silent -u USER:PASSWORD --show-error --data 'json={"parameter":[{"name":"PARAMNAME","label":"mylabel"}]}&Submit=Build' http://localhost:8080/job/remote/build?token=SECTOKEN
```

It is also possible to pass the parameter via GET (example: NODENAME is
a 'Node' parameter defined on the job):

``` syntaxhighlighter-pre
 http://localhost:8080/jenkins/job/MyJob/buildWithParameters?NODENAME=node1
```

## Parameterized Trigger plugin

#### Post Build Action

If the [Parameterized Trigger
Plugin](https://wiki.jenkins.io/display/JENKINS/Parameterized+Trigger+Plugin)
is installed, an additional parameter is available to pass to the target
job. The parameterized trigger plugin handles Node and Label parameters
as every other parameter if you use the option 'Current build
parameters'.  
But it is not possible to use the 'Predefined parameters' to overwrite
such a parameter, therefore the NodeLabel Parameter plugin adds a new
parameter to the trigger plugin.

This parameter type defines where the target job should be executed, the
value must match either a label or a node name - otherwise the job will
just stay in the queue. The NodeLabel parameter passed to the target
job, does not have to exist on the target job (but if the target has one
defined, it should match the name). This way it is possible to trigger
jobs on different nodes then they are actually configured.  
![](https://wiki.jenkins.io/download/attachments/57183146/parameterized-trigger-param.jpg?version=1&modificationDate=1310306213000&api=v2)

#### BuildParameterFactory

The nodelabel parameter plugin also adds a `BuildParameterFactory` to
the parameterized trigger plugin, this factory enables you to trigger a
build of a specific project on all nodes having the same label.

1.  Add the a "Trigger/call builds on other projects" build step
2.  define the project you want to run on each node
3.  select the "All Nodes for Label Factory" from the "Add
    ParameterFactory" dropdown
4.  define the label identifying all the nodes you want to run the
    project on

![](https://wiki.jenkins.io/download/attachments/57183146/screen-capture-4.jpg?version=1&modificationDate=1321719491000&api=v2)

Similarly, you can also add "Build on every online node" as a parameter
factory. This will cause the specified projects to run on all nodes
(master and all slaves) that are online and have non-zero executor
configured to it.

# Version History

### 1.7 (7. Dec. 2015)

-   fix [JENKINS-22185](https://issues.jenkins-ci.org/browse/JENKINS-22185)
    Trigger a build in all nodes matching a label (thanks to Antonio)

### 1.6 (23. Nov. 2015)

-   add MIT license [PR
    \#8](https://github.com/jenkinsci/nodelabelparameter-plugin/pull/8)
    (thanks vaneyckt)
-   fix [JENKINS-28374](https://issues.jenkins-ci.org/browse/JENKINS-28374)
    enable triggering via rebuild plugin (thanks lrobertson39)
-   fix [JENKINS-27880](https://issues.jenkins-ci.org/browse/JENKINS-27880)
    expose value in API (thanks Charles Stephens)
-   fix typo [PR
    \#4](https://github.com/jenkinsci/nodelabelparameter-plugin/pull/4)
    (thanks guysoft)

### 1.5.1 (2. March 2014)

-   fix
    [JENKINS-21828](https://issues.jenkins-ci.org/browse/JENKINS-21828)
    encoding issues with german "umlaute"
-   integrate [PR
    \#3](https://github.com/jenkinsci/nodelabelparameter-plugin/pull/3) 
    LabelParameterDefinition extends SimpleParameterDefinition to
    support better integration with other plugins

### 1.5.0 (10. Feb 2014)

-   implement [JENKINS-17660](https://issues.jenkins-ci.org/browse/JENKINS-17660)
    for convenience, allow usage of 'value' for calls of label
    parameters via script
-   implement [JENKINS-21194](https://issues.jenkins-ci.org/browse/JENKINS-21194)
    add notion of NodeEligibility to differentiate between different
    offline modes of nodes
-   new feature: "*build on all nodes matching label*" for Label
    parameter
-   fix [JENKINS-20885](https://issues.jenkins-ci.org/browse/JENKINS-20885)
    default node not selected in dropdown
-   fix [JENKINS-20823](https://issues.jenkins-ci.org/browse/JENKINS-20823)
    Stack Trace when Configuring a task with "Label Factory"
-   lift dependency to core 1.509.4

### 1.4 (28. March 2013)

-   implement
    [JENKINS-17305](https://issues.jenkins-ci.org/browse/JENKINS-17305)
    don't wait for offline nodes (new option added for node parameter
    and parameter factory)
-   implement
    [JENKINS-14407](https://issues.jenkins-ci.org/browse/JENKINS-14407)
    allow multi-value default for scheduled builds

### 1.3 (9. Dec 2012)

-   Add new build parameter factory for ﻿[Parameterized Trigger
    Plugin](https://wiki.jenkins.io/display/JENKINS/Parameterized+Trigger+Plugin)
    : List of nodes - factory takes a list of nodes to trigger the job
    on
-   fix
    [JENKINS-15339](https://issues.jenkins-ci.org/browse/JENKINS-15339)
    breaks Windows batch file build steps if Name field not filled in
-   fix
    [JENKINS-15370](https://issues.jenkins-ci.org/browse/JENKINS-15370)
    Value of Node param variable does not contain all values when
    selecting multiple nodes

### 1.2.1 (2. July 2012)

-   fixed
    [JENKINS-14230](https://issues.jenkins-ci.org/browse/JENKINS-14230)
    Node parameter value can't be passed by HTTP GET

### 1.2 (25. June 2012)

-   fix
    [JENKINS-14120](https://issues.jenkins-ci.org/browse/JENKINS-14120)
    confusing output
-   fix
    [JENKINS-13902](https://issues.jenkins-ci.org/browse/JENKINS-13902)
    move 'master' up in selection list
-   fix
    [JENKINS-14109](https://issues.jenkins-ci.org/browse/JENKINS-14109)
    Passing ${NODE\_NAME} as Node Parameter to downstream job not
    possible on master

### 1.1.4 (21. May 2012)

-   supported a build parameter factory that lets you run a specified
    job on all the slaves.

### 1.1.3 (11. May 2012)

-   fix [JENKINS-13704](https://issues.jenkins-ci.org/browse/JENKINS-13704)
    Unable to start a concurrent build when there are
    nodeParameterValues

### 1.1.2 (25. Feb. 2011)

-   fix
    [JENKINS-12226](https://issues.jenkins-ci.org/browse/JENKINS-12226)
    Triggering a build with "Current build parameters" fails when the
    current build parameters includes a node name
-   fix issue if a a job has many parameters

### 1.1.1 (27. Nov. 2011)

-   Fix issue if a label was using expressions

### 1.1.0 (15. Nov. 2011)

-   Added a BuildParameterFactory to get all Nodes for a Label when
    triggered via buildstep from "parameterized-trigger-plugin" ([pull
    \#1](https://github.com/jenkinsci/nodelabelparameter-plugin/pull/1),
    thanks to wolfs)

### 1.0.0 (18. Sep. 2011)

-   fix
    [JENKINS-10982](https://issues.jenkins-ci.org/browse/JENKINS-10982)
    trim labels / nodes
-   fix
    [JENKINS-11006](https://issues.jenkins-ci.org/browse/JENKINS-11006)
    fix remote triggering (e.g. via script)
-   enhance node parameter to support concurrent job execution
-   ...in case you have an issue after updating to this version, try to
    just open the job configuration and save it again.

### 0.2.0

-   add badge icon to show on which node/label the build was done
-   add support to execute the job on multiple nodes automatically  (one
    after the other)

### 0.1.2

-   add additional parameter to parameterized-trigger plugin
    ([JENKINS-10088](https://issues.jenkins-ci.org/browse/JENKINS-10088))

### 0.1.0

-   Inital

# Help and Support

type

key

summary

Data cannot be retrieved due to an unexpected error.

[View these issues in
Jira](http://issues.jenkins-ci.org/secure/IssueNavigator.jspa?reset=true&jqlQuery=project%20=%20JENKINS%20AND%20status%20in%20%28Open,%20%22In%20Progress%22,%20Reopened%29%20AND%20component%20=%20%27nodelabelparameter-plugin%27&src=confmacro)

For Help and support please use the [Jenkins
Users](http://jenkins-ci.org/content/mailing-lists) mailing list.  
The comment list below is not monitored.

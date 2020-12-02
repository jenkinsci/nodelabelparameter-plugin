# Node Label Parameter Plugin for Jenkins

[![Build Status](https://ci.jenkins.io/job/Plugins/job/nodelabelparameter-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/nodelabelparameter-plugin/job/master/)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/nodelabelparameter-plugin.svg)](https://github.com/jenkinsci/nodelabelparameter-plugin/graphs/contributors)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/nodelabelparameter.svg)](https://plugins.jenkins.io/nodelabelparameter)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/nodelabelparameter.svg?color=blue)](https://plugins.jenkins.io/nodelabelparameter)

This plugin adds two new parameter types to job configuration - node and label, this allows to
dynamically select the node where a job/project should be executed.

## Description

The plugin allows to configure additional parameters for a job. These new parameter types are **'Node'**
and **'Label'**. This is specially useful if you want to execute the job on different nodes without
changing the configuration over and over again. It also allows you to use Jenkins in a scenario where
you would like to setup different nodes with the same script/jobs configured - e.g. SW provisioning.

Another usage scenario would be to configure a node maintenance job which you could trigger on request
on each node.

![](https://wiki.jenkins.io/download/attachments/57183146/selectParameter.jpg?version=1&modificationDate=1308076539000&api=v2)

#### Restrict

If your using a node or label parameter to run your job on a particular node, you should not use the
option **"Restrict where this project can be run"** in the job configuration - it will not have
any effect to the selection of your node anymore!

#### Node

Define a list of nodes on which the job should be allowed to be executed on. A default node
used for scheduled jobs can be defined.  You are able to configure the job to run one after the other
or even concurrent.
>>>>>>> Stashed changes

![](https://wiki.jenkins.io/download/attachments/57183146/config_plugin.jpg?version=1&modificationDate=1316361239000&api=v2)

In case multi node selection was disabled, you get a dropdown to select one node to execute the job on.

![](https://wiki.jenkins.io/download/attachments/57183146/triggerWithNode.jpg?version=1&modificationDate=1308076539000&api=v2)

If multi node selection was enabled, you get the chance to select multiple nodes to run the job on.
The job will then be executed on each of the nodes, one after the other or concurrent - depending on the
configuration.

![](https://wiki.jenkins.io/download/attachments/57183146/multinode_selection.jpg?version=1&modificationDate=1315766641000&api=v2)

#### Label

Define a label of **'Restrict where this project can be run'** on the fly.

![](https://wiki.jenkins.io/download/attachments/57183146/labelParameter.jpg?version=1&modificationDate=1308076539000&api=v2)

![](https://wiki.jenkins.io/download/attachments/57183146/triggerWithLabel.jpg?version=1&modificationDate=1308076539000&api=v2)

#### Trigger via script

One can also trigger a job via remote call (e.g. script).

Trigger job on multiple nodes:

```sh
curl --silent -u USER:PASSWORD --show-error --data 'json={"parameter":[{"name":"PARAMNAME","value":["node1","node2"]}]}&Submit=Build' http://localhost:8080/job/remote/build?token=SECTOKEN
```

Although the first format also supports passing just one node name as parameter in the list, the
plugin also supports to pas a simple key/value parameter to trigger the job on  single node only:

```sh
curl --silent -u USER:PASSWORD --show-error --data 'json={"parameter":[{"name":"PARAMNAME","value":"master"}]}&Submit=Build' http://localhost:8080/job/remote/build?token=SECTOKEN
```

If you have a 'label' parameter (instead of a 'node' parameter), then the request should look like this:

```sh
curl --silent -u USER:PASSWORD --show-error --data 'json={"parameter":[{"name":"PARAMNAME","label":"mylabel"}]}&Submit=Build' http://localhost:8080/job/remote/build?token=SECTOKEN
```

It is also possible to pass the parameter via GET (example: NODENAME is a 'Node' parameter defined on the job):

```sh
http://localhost:8080/jenkins/job/MyJob/buildWithParameters?NODENAME=node1
```

## Parameterized Trigger plugin

#### Post Build Action

If the [Parameterized Trigger Plugin](https://wiki.jenkins.io/display/JENKINS/Parameterized+Trigger+Plugin)
is installed, an additional parameter is available to pass to the target job. The parameterized
trigger plugin handles Node and Label parameters as every other parameter if you use the option **'Current build
parameters'**.

But it is not possible to use the **'Predefined parameters'** to overwrite such a parameter,
therefore the NodeLabel Parameter plugin adds a new parameter to the trigger plugin.

This parameter type defines where the target job should be executed, the value must match either a
label or a node name - otherwise the job will just stay in the queue. The NodeLabel parameter passed
to the target job, does not have to exist on the target job (but if the target has one
defined, it should match the name). This way it is possible to trigger jobs on different nodes then
they are actually configured.

![](https://wiki.jenkins.io/download/attachments/57183146/parameterized-trigger-param.jpg?version=1&modificationDate=1310306213000&api=v2)

#### BuildParameterFactory

The nodelabel parameter plugin also adds a `BuildParameterFactory` to the parameterized trigger plugin,
this factory enables you to trigger a build of a specific project on all nodes having the same label.

- Add the a **"Trigger/call builds on other projects"** build step.
- Define the project you want to run on each node.
- Select the "All Nodes for Label Factory" from the **"Add ParameterFactory"** dropdown.
- Define the label identifying all the nodes you want to run the project on.

![](https://wiki.jenkins.io/download/attachments/57183146/screen-capture-4.jpg?version=1&modificationDate=1321719491000&api=v2)

Similarly, you can also add "Build on every online node" as a parameter factory. This will cause
the specified projects to run on all nodes (master and all slaves) that are online and have non-zero executor
configured to it.

## Help and Support

[View these issues in JIRA](http://issues.jenkins-ci.org/secure/IssueNavigator.jspa?reset=true&jqlQuery=project%20=%20JENKINS%20AND%20status%20in%20%28Open,%20%22In%20Progress%22,%20Reopened%29%20AND%20component%20=%20%27nodelabelparameter-plugin%27&src=confmacro)

For Help and support please use the [Jenkins Users](http://jenkins-ci.org/content/mailing-lists) mailing list.
The comment list below is not monitored.

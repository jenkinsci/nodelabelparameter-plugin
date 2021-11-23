# Version History

Recent releases are described in [GitHub Releases](https://github.com/jenkinsci/nodelabelparameter-plugin/releases).

## 1.7.3 (28. Oct. 2016)

-   fix [JENKINS-15339](https://issues.jenkins.io/browse/JENKINS-15339)
    and[JENKINS-24280](https://issues.jenkins.io/browse/JENKINS-24280)
    Make sure node name is not empty
-   fix [JENKINS-30506](https://issues.jenkins.io/browse/JENKINS-30506)
    loading of nodeEligibility

## 1.7.2 (30. Mar. 2016)

-   fix [JENKINS-32939](https://issues.jenkins.io/browse/JENKINS-32939)
    change the constructor when asking for default value.

## 1.7.1 (30. Mar. 2016)

-   fix [JENKINS-32209](https://issues.jenkins.io/browse/JENKINS-32209)
    Override ParameterValue.buildEnvironment()

## 1.7 (7. Dec. 2015)

-   fix [JENKINS-22185](https://issues.jenkins.io/browse/JENKINS-22185)
    Trigger a build in all nodes matching a label (thanks to Antonio)

## 1.6 (23. Nov. 2015)

-   add MIT license [PR-8](https://github.com/jenkinsci/nodelabelparameter-plugin/pull/8)
    (thanks vaneyckt)
-   fix [JENKINS-28374](https://issues.jenkins.io/browse/JENKINS-28374)
    enable triggering via rebuild plugin (thanks lrobertson39)
-   fix [JENKINS-27880](https://issues.jenkins.io/browse/JENKINS-27880)
    expose value in API (thanks Charles Stephens)
-   fix typo [PR-4](https://github.com/jenkinsci/nodelabelparameter-plugin/pull/4)
    (thanks guysoft)

## 1.5.1 (2. March 2014)

-   fix [JENKINS-21828](https://issues.jenkins.io/browse/JENKINS-21828)
    encoding issues with german "umlaute"
-   integrate [PR-3](https://github.com/jenkinsci/nodelabelparameter-plugin/pull/3)
    LabelParameterDefinition extends SimpleParameterDefinition to
    support better integration with other plugins

## 1.5.0 (10. Feb 2014)

-   implement[JENKINS-17660](https://issues.jenkins.io/browse/JENKINS-17660)
    for convenience, allow usage of 'value' for calls of label
    parameters via script
-   implement[JENKINS-21194](https://issues.jenkins.io/browse/JENKINS-21194)
    add notion of NodeEligibility to differentiate between different
    offline modes of nodes
-   new feature: "*build on all nodes matching label*" for Label
    parameter
-   fix [JENKINS-20885](https://issues.jenkins.io/browse/JENKINS-20885)
    default node not selected in dropdown
-   fix [JENKINS-20823](https://issues.jenkins.io/browse/JENKINS-20823)
    Stack Trace when Configuring a task with "Label Factory"
-   lift dependency to core 1.509.4

## 1.4 (28. March 2013)

-   implement [JENKINS-17305](https://issues.jenkins.io/browse/JENKINS-17305)
    don't wait for offline nodes (new option added for node parameter
    and parameter factory)
-   implement [JENKINS-14407](https://issues.jenkins.io/browse/JENKINS-14407)
    allow multi-value default for scheduled builds

## 1.3 (9. Dec 2012)

-   Add new build parameter factory for
    [Parameterized Trigger Plugin](https://plugins.jenkins.io/parameterized-trigger/):
    List of nodes - factory takes a list of nodes to trigger the job on
-   fix [JENKINS-15339](https://issues.jenkins.io/browse/JENKINS-15339)
    breaks Windows batch file build steps if Name field not filled in
-   fix [JENKINS-15370](https://issues.jenkins.io/browse/JENKINS-15370)
    Value of Node param variable does not contain all values when
    selecting multiple nodes

## 1.2.1 (2. July 2012)

-   fix [JENKINS-14230](https://issues.jenkins.io/browse/JENKINS-14230)
    Node parameter value can't be passed by HTTP GET

## 1.2 (25. June 2012)

-   fix [JENKINS-14120](https://issues.jenkins.io/browse/JENKINS-14120)
    confusing output
-   fix [JENKINS-13902](https://issues.jenkins.io/browse/JENKINS-13902)
    move 'master' up in selection list
-   fix [JENKINS-14109](https://issues.jenkins.io/browse/JENKINS-14109)
    Passing `${NODE_NAME}` as Node Parameter to downstream job not
    possible on master

## 1.1.4 (21. May 2012)

-   supported a build parameter factory that lets you run a specified
    job on all the agents.

## 1.1.3 (11. May 2012)

-   fix [JENKINS-13704](https://issues.jenkins.io/browse/JENKINS-13704)
    Unable to start a concurrent build when there are
    nodeParameterValues

## 1.1.2 (25. Feb. 2011)

-   fix [JENKINS-12226](https://issues.jenkins.io/browse/JENKINS-12226)
    Triggering a build with "Current build parameters" fails when the
    current build parameters includes a node name
-   fix issue if a a job has many parameters

## 1.1.1 (27. Nov. 2011)

-   Fix issue if a label was using expressions

## 1.1.0 (15. Nov. 2011)

-   Added a BuildParameterFactory to get all Nodes for a Label when
    triggered via buildstep from "parameterized-trigger-plugin"
    ([PR-1](https://github.com/jenkinsci/nodelabelparameter-plugin/pull/1),
    thanks to wolfs)

## 1.0.0 (18. Sep. 2011)

-   fix [JENKINS-10982](https://issues.jenkins.io/browse/JENKINS-10982)
    trim labels / nodes
-   fix [JENKINS-11006](https://issues.jenkins.io/browse/JENKINS-11006)
    fix remote triggering (e.g. via script)
-   enhance node parameter to support concurrent job execution
    ...in case you have an issue after updating to this version, try to
    just open the job configuration and save it again.

## 0.2.0

-   add badge icon to show on which node/label the build was done
-   add support to execute the job on multiple nodes automatically (one
    after the other)

## 0.1.2

-   add additional parameter to parameterized-trigger plugin
    [JENKINS-10088](https://issues.jenkins.io/browse/JENKINS-10088)

## 0.1.0

-   Inital

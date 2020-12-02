# nodelabelparameter-plugin Changelog

## 1.7 (7. Dec. 2015)

### Fixed

* fix [https://issues.jenkins-ci.org/browse/JENKINS-22185](JENKINS-22185): Trigger a build in all
nodes matching a label (thanks to Antonio).

## 1.6 (23. Nov. 2015)

### Added

* add MIT license [https://github.com/jenkinsci/nodelabelparameter-plugin/pull/8](PR #8)
(thanks vaneyckt)

### Fixed

* fix [https://issues.jenkins-ci.org/browse/JENKINS-28374](JENKINS-28374):enable triggering via
* rebuild plugin (thanks lrobertson39).
* fix [https://issues.jenkins-ci.org/browse/JENKINS-27880](JENKINS-27880): expose value in API (thanks Charles Stephens)
* fix typo [https://github.com/jenkinsci/nodelabelparameter-plugin/pull/4](PR #4) (thanks guysoft)

## 1.5.1 (2. March 2014)

### Added

* integrate [https://github.com/jenkinsci/nodelabelparameter-plugin/pull/3](PR #3): LabelParameterDefinition
extends SimpleParameterDefinition to support better integration with other plugins.

### Fixed

* fix [https://issues.jenkins-ci.org/browse/JENKINS-21828](JENKINS-21828): encoding issues with german "umlaute".

## 1.5.0 (10. Feb 2014)

### Added

* new feature: "_build on all nodes matching label_" for Label parameter.

### Changed

* implement [https://issues.jenkins-ci.org/browse/JENKINS-17660](JENKINS-17660): for convenience,
allow usage of 'value' for calls of label parameters via script.
* implement [https://issues.jenkins-ci.org/browse/JENKINS-21194](JENKINS-21194): add notion of
NodeEligibility to differentiate between different offline modes of nodes.
* lift dependency to core 1.509.4.

### Fixed

* fix [https://issues.jenkins-ci.org/browse/JENKINS-20885](JENKINS-20885): default node not selected
in dropdown
* fix [https://issues.jenkins-ci.org/browse/JENKINS-20823](JENKINS-20823): Stack Trace when configuring
a task with "Label Factory"

## 1.4 (28. March 2013)

### Changed

* implement [https://issues.jenkins-ci.org/browse/JENKINS-17305](JENKINS-17305): don't wait for
offline nodes (new option added for node parameter and parameter factory)
* implement [https://issues.jenkins-ci.org/browse/JENKINS-14407](JENKINS-14407): allow multi-value
default for scheduled builds

## 1.3 (9. Dec 2012)

### Added

Add new build parameter factory for Parameterized Trigger Plugin: List of nodes - factory takes a
list of nodes to trigger the job on.

### Fixed

* fix [https://issues.jenkins-ci.org/browse/JENKINS-15339](JENKINS-15339): breaks Windows batch file
build steps if Name field not filled in.
* fix [https://issues.jenkins-ci.org/browse/JENKINS-15370](JENKINS-15370): Value of Node param variable
does not contain all values when selecting multiple nodes.

## 1.2.1 (2. July 2012)

### Fixed

* fixed [https://issues.jenkins-ci.org/browse/JENKINS-14230](JENKINS-14230): Node parameter value
can't be passed by HTTP GET

## 1.2 (25. June 2012)

### Fixed

* fix [https://issues.jenkins-ci.org/browse/JENKINS-14120](JENKINS-14120): confusing output
* fix [https://issues.jenkins-ci.org/browse/JENKINS-13902](JENKINS-13902): move 'master' up in selection list
* fix [https://issues.jenkins-ci.org/browse/JENKINS-14109](JENKINS-14109): Passing $\{NODE_NAME} as Node Parameter to downstream job not
possible on master

## 1.1.4 (21. May 2012)

### Added

* supported a build parameter factory that lets you run a specified job on all the slaves.

## 1.1.3 (11. May 2012)

### Fixed

* fix [https://issues.jenkins-ci.org/browse/JENKINS-13704](JENKINS-13704): Unable to start a concurrent build when there are
nodeParameterValues

## 1.1.2 (25. Feb. 2011)

### Fixed

* fix [https://issues.jenkins-ci.org/browse/JENKINS-12226](JENKINS-12226): Triggering a build with
"Current build parameters" fails when the current build parameters includes a node name
* fix issue if a a job has many parameters

## 1.1.1 (27. Nov. 2011)

### Fixed

* Fix issue if a label was using expressions

## 1.1.0 (15. Nov. 2011)

### Added

* Added a BuildParameterFactory to get all Nodes for a Label when triggered via buildstep from
"parameterized-trigger-plugin" [https://github.com/jenkinsci/nodelabelparameter-plugin/pull/1](pull #1,thanks to wolfs)

## 1.0.0 (18. Sep. 2011)

### Changed

* enhance node parameter to support concurrent job execution
* ...in case you have an issue after updating to this version, try to just open the job configuration
and save it again.

### Fixed

* fix [https://issues.jenkins-ci.org/browse/JENKINS-10982](JENKINS-10982) trim labels / nodes
* fix [https://issues.jenkins-ci.org/browse/JENKINS-11006](JENKINS-11006) fix remote triggering (e.g. via script)

## 0.2.0

### Added

* add badge icon to show on which node/label the build was done
* add support to execute the job on multiple nodes automatically (one after the other)

## 0.1.2

### Added

* add additional parameter to parameterized-trigger plugin
[https://issues.jenkins-ci.org/browse/JENKINS-10088](JENKINS-10088)

## 0.1.0

* Inital

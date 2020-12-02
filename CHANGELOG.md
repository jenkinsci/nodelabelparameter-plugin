# nodelabelparameter-plugin Changelog

## [Unreleased]

## [1.7] - 2015-12-07

### Fixed
- [JENKINS-22185](https://issues.jenkins-ci.org/browse/JENKINS-22185): Trigger a build in all
nodes matching a label (thanks to Antonio).

## [1.6] - 2015-11-23

### Added
- MIT license [PR #8](https://github.com/jenkinsci/nodelabelparameter-plugin/pull/8) (thanks vaneyckt).

### Fixed
- [JENKINS-28374](https://issues.jenkins-ci.org/browse/JENKINS-28374):enable triggering via  rebuild plugin (thanks lrobertson39).
- [JENKINS-27880](https://issues.jenkins-ci.org/browse/JENKINS-27880): expose value in API (thanks Charles Stephens).
- Typo: [PR #4](https://github.com/jenkinsci/nodelabelparameter-plugin/pull/4) (thanks guysoft).

## [1.5.1] - 2014-03-02

### Added
- [PR #3](https://github.com/jenkinsci/nodelabelparameter-plugin/pull/3): `LabelParameterDefinition`
extends `SimpleParameterDefinition` to support better integration with other plugins.

### Fixed
- [JENKINS-21828](https://issues.jenkins-ci.org/browse/JENKINS-21828): encoding issues with german "umlaute".

## [1.5.0] - 2014-02-10

### Added
- Feature: "_build on all nodes matching label_" for **Label** parameter.

### Changed
- [JENKINS-17660](https://issues.jenkins-ci.org/browse/JENKINS-17660): For convenience, allow usage
of 'value' for calls of label parameters via script.
- [JENKINS-21194](https://issues.jenkins-ci.org/browse/JENKINS-21194): Add notion of `NodeEligibility`
to differentiate between different offline modes of nodes.
- Lift dependency to core 1.509.4.

### Fixed
- [JENKINS-20885](https://issues.jenkins-ci.org/browse/JENKINS-20885): Default node not selected
in dropdown.
- [JENKINS-20823](https://issues.jenkins-ci.org/browse/JENKINS-20823): Stack trace when configuring
a task with "Label Factory".

## [1.4] - 2013-03-28

### Changed
- [JENKINS-17305](https://issues.jenkins-ci.org/browse/JENKINS-17305): Don't wait for
offline nodes (new option added for node parameter and parameter factory).
- [JENKINS-14407](https://issues.jenkins-ci.org/browse/JENKINS-14407): Allow multi-value
default for scheduled builds.

## [1.3] - 2012-12-09

### Added
- New build parameter factory for Parameterized Trigger Plugin: List of nodes - factory takes a
list of nodes to trigger the job on.

### Fixed
- [JENKINS-15339](https://issues.jenkins-ci.org/browse/JENKINS-15339): Breaks Windows batch file
build steps if **Name** field not filled in.
- [JENKINS-15370](https://issues.jenkins-ci.org/browse/JENKINS-15370): Value of **Node** param variable
does not contain all values when selecting multiple nodes.

## [1.2.1] - 2012-07-02

### Fixed
- [JENKINS-14230](https://issues.jenkins-ci.org/browse/JENKINS-14230): Node parameter value
can't be passed by HTTP GET.

## [1.2] - 2012-06-25

### Fixed
- fix [JENKINS-14120](https://issues.jenkins-ci.org/browse/JENKINS-14120): Confusing output.
- fix [JENKINS-13902](https://issues.jenkins-ci.org/browse/JENKINS-13902): Move 'master' up in selection list.
- fix [JENKINS-14109](https://issues.jenkins-ci.org/browse/JENKINS-14109): Passing $\{NODE_NAME} as Node Parameter to downstream job not
possible on master.

## [1.1.4] - 2012-05-21

### Added
- A build parameter factory that lets you run a specified job on all the slaves.

## [1.1.3] - 2012-05-11

### Fixed
- [JENKINS-13704](https://issues.jenkins-ci.org/browse/JENKINS-13704): Unable to start a concurrent build when there are
`nodeParameterValues`.

## [1.1.2] - 2011-02-25

### Fixed
- [JENKINS-12226](https://issues.jenkins-ci.org/browse/JENKINS-12226): Triggering a build with
"Current build parameters" fails when the current build parameters includes a node name.
- Issue if a a job has too many parameters.

## [1.1.1 ] - 2011-11-27

### Fixed
- Issue if a label was using expressions.

## [1.1.0] - 2011-11-15

### Added
- BuildParameterFactory to get all Nodes for a Label when triggered via buildstep from
[parameterized-trigger-plugin](https://github.com/jenkinsci/parameterized-trigger-plugin)
[pull #1,thanks to wolfs](https://github.com/jenkinsci/nodelabelparameter-plugin/pull/1).

## [1.0.0] - 2011-09-18

### Changed
- Enhance node parameter to support concurrent job execution.
- In case you have an issue after updating to this version, try to just open the job configuration
and save it again.

### Fixed
- [JENKINS-10982](https://issues.jenkins-ci.org/browse/JENKINS-10982): Trim labels / nodes.
- [JENKINS-11006](https://issues.jenkins-ci.org/browse/JENKINS-11006): Remote triggering (e.g. via script).

## [0.2.0] - 2011-07-10

### Added
- Badge icon to show on which node/label the build was done.
- Support to execute the job on multiple nodes automatically (one after the other).

## [0.1.2] - 2011-07-10

### Added
- Additional parameter to parameterized-trigger plugin [JENKINS-10088](https://issues.jenkins-ci.org/browse/JENKINS-10088).

## [0.1.0] - 2011-06-22
- Initial release.

[Unreleased]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.7...HEAD
[1.7]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.6...nodelabelparameter-1.7
[1.6]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.5.1...nodelabelparameter-1.6
[1.5.1]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.5.0...nodelabelparameter-1.5.1
[1.5.0]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.4...nodelabelparameter-1.5.0
[1.4]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.3...nodelabelparameter-1.4
[1.3]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.2.1...nodelabelparameter-1.3
[1.2.1]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.2...nodelabelparameter-1.2.1
[1.2]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.1.4...nodelabelparameter-1.2
[1.1.4]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.1.3...nodelabelparameter-1.1.4
[1.1.3]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.1.2...nodelabelparameter-1.1.3
[1.1.2]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.1.1...nodelabelparameter-1.1.2
[1.1.1]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.1.0...nodelabelparameter-1.1.1
[1.1.0]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-1.0.0...nodelabelparameter-1.1.0
[1.0.0]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-0.2.0...nodelabelparameter-1.0.0
[0.2.0]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-0.1.2...nodelabelparameter-0.2.0
[0.1.2]: https://github.com/jenkinsci/nodelabelparameter-plugin/compare/nodelabelparameter-0.1.0...nodelabelparameter-0.1.2
[0.1.0]: https://github.com/jenkinsci/nodelabelparameter-plugin/releases/tag/nodelabelparameter-0.1.0

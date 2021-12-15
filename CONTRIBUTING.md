# Contributing to the Node and Label Parameter plugin

Plugin source code is hosted on [GitHub](https://github.com/jenkinsci/nodelabelparameter-plugin/).
New feature proposals and bug fix proposals should be submitted as
[GitHub pull requests](https://help.github.com/articles/creating-a-pull-request).
Your pull request will be evaluated by the [Jenkins job](https://ci.jenkins.io/job/Plugins/job/nodelabelparameter-plugin/).

Before submitting your change, please assure that you've added tests which verify your change.

## Code Coverage

Code coverage reporting is available as a maven target.
Please try to improve code coverage with tests when you submit.
* `mvn -P enable-jacoco clean verify jacoco:report` to report code coverage

Please don't introduce new spotbugs output.
* `mvn spotbugs:check` to analyze project using [Spotbugs](https://spotbugs.github.io)
* `mvn spotbugs:gui` to review report using GUI

## Report an issue

Report issues and enhancements with the link:https://www.jenkins.io/participate/report-issue/redirect/#15873[Jenkins issue tracker].
Please use the link:https://www.jenkins.io/participate/report-issue/["How to Report an Issue"] guidelines when reporting issues.

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

## Fetching code coverage

Once you get the report code coverage, there are a couple ways you can fetch the classes that are covered in tests.

This depends a lot on the operating system and environment. The following commands will open the `index.html` file in the browser.

* Windows - `target/site/jacoco/index.html`
* Linux - `xdg-open target/site/jacoco/index.html`
* Gitpod - `cd target/site/jacoco && python -m http.server 8000`

The file will have a list of package names. You can click on them to find a list of class names.

The lines of the code will be covered in three different colors. Red, green, and orange.

The red lines are not covered in the tests. The green lines are covered with tests. 

Your goal is to add to tests that covers one or more of the red lines with the new tests.





## Report an issue

Report issues and enhancements with the link:https://www.jenkins.io/participate/report-issue/redirect/#15873[Jenkins issue tracker].
Please use the link:https://www.jenkins.io/participate/report-issue/["How to Report an Issue"] guidelines when reporting issues.


This gradle plugin provides the ability to generate a git properties file in 
the same format as the [maven-git-commit-id-plugin](https://github.com/ktoso/maven-git-commit-id-plugin).

This plugin contains some modified source code from the original maven plugin (GNU Lesser General Public License 3.0).

Usages:

```groovy
    gitVersionPlugin {
      destinationFile = file("src/main/resources/META-INF/version/git-${group}_${project.name}.properties")
      gitDir = file("/home/SMB01/andrew.lee/tmp/ut/utilities-all/.git")
    }
```

Notes:

This requires the version to be set before this plugin runs. 

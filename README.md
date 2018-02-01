# Git Version Gradle Plugin
This gradle plugin provides the ability to generate a git properties file in 
the same format as the [maven-git-commit-id-plugin](https://github.com/ktoso/maven-git-commit-id-plugin).

This plugin contains some modified source code from the original maven plugin (GNU Lesser General Public License 3.0).


## Usage
```groovy
    gitVersionPlugin {
      destinationFile = file("src/main/resources/META-INF/version/git-${group}_${project.name}.properties")
      gitDir = file("${rootProject.projectDir}/.git")
    }
```

## Notes
This requires the version to be set before this plugin runs. 

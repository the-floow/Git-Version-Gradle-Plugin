# Git Version Gradle Plugin
[![Build Status](https://travis-ci.org/the-floow/Git-Version-Gradle-Plugin.svg?branch=master)](https://travis-ci.org/the-floow/Git-Version-Gradle-Plugin)
[ ![Download](https://api.bintray.com/packages/the-floow/gradle-plugins/com.thefloow%3Agit-version-gradle-plugin/images/download.svg) ](https://bintray.com/the-floow/gradle-plugins/com.thefloow%3Agit-version-gradle-plugin/_latestVersion)

This gradle plugin provides the ability to generate a properties file containing git version information in 
the same format as the [maven-git-commit-id-plugin](https://github.com/ktoso/maven-git-commit-id-plugin). This plugin contains some modified source code from the original maven plugin.

This properties file is generated when the build runs.

An example of the properties file is as follows:

    git.build.user.email=user@example.com
    git.build.host=mylaptop
    git.dirty=false
    git.remote.origin.url=ssh\://git@exampke.com/repository.git
    git.closest.tag.name=1.0.0
    git.commit.id.describe-short=ee93bfe
    git.commit.user.email=user@example.com
    git.commit.time=01.02.2018 @ 08\:39\:15 GMT
    git.commit.message.full=Adding readme file
    git.build.version=1.0
    git.commit.message.short=Adding readme file
    git.commit.id.abbrev=ee93bfe
    git.branch=master
    git.build.user.name=John Smith
    git.closest.tag.commit.count=0
    git.commit.id.describe=ee93bfe
    git.commit.id=ee93bfea8bccf0c22367f22d289b176819bec214
    git.tags=1.0.0
    git.build.time=03.02.2018 @ 14\:52\:44 GMT
    git.commit.user.name=John Smith

## Usage
Add the plugin as a build script dependency:
```groovy
buildscript {
  dependencies {
    classpath 'com.thefloow:git-version-gradle-plugin:1.0.5'
  }
}
```

Configure the plugin to specify a destination file path and the git directory:
```groovy
gitVersionPlugin {
  destinationFile = file("src/main/resources/META-INF/version/git-${group}_${project.name}.properties")
  gitDir = file("${rootProject.projectDir}/.git")
}
```

## Notes
This requires the version to be set before this plugin runs. 

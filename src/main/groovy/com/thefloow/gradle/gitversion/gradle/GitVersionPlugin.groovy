package com.thefloow.gradle.gitversion.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitVersionPlugin implements Plugin<Project> {

    void apply(Project project) {

        def extension = project.extensions.create('gitVersionPlugin', GitVersionPluginExtension, project)

        project.tasks.create('generatePropertiesFile', GeneratePropertyFileTask) {
            destinationFile = { extension.destinationFile }
            gitDir = { extension.gitDir }
        }
    }
}


package com.thefloow.gradle.gitversion.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class GitVersionPlugin implements Plugin<Project> {

    void apply(Project project) {

        def extension = project.extensions.create('gitVersionPlugin', GitVersionPluginExtension, project)

        def task = project.tasks.create('generatePropertiesFile', GeneratePropertyFileTask) {
            destinationFile = { extension.destinationFile }
            gitDir = { extension.gitDir }
        }

        project.getPlugins().withType(JavaPlugin) {
                project.tasks.processResources.dependsOn task
        }

    }
}


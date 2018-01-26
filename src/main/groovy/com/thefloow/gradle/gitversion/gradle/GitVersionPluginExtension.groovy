package com.thefloow.gradle.gitversion.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property

class GitVersionPluginExtension {

    final Property<File> destinationFile
    final Property<File> gitDir

    GitVersionPluginExtension(Project project) {
        destinationFile = project.objects.property(File)
        gitDir = project.objects.property(File)
    }
}
package com.thefloow.gradle.gitversion.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property

class GitVersionPluginExtension {

    final Property<File> destinationFile
    final Property<File> gitDir

    GitVersionPluginExtension(Project project) {
        destinationFile = project.objects.property(File)
        destinationFile.set(new File("META-INF/version/git-${project.group}_${project.name}.properties"))

        gitDir = project.objects.property(File)
        gitDir.set(project.rootProject.file(".git"))
    }
}
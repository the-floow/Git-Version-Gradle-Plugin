package com.thefloow.gradle.gitversion.gradle

import com.thefloow.gradle.gitversion.VersionFileGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GeneratePropertyFileTask extends DefaultTask {

    def destinationFile
    def gitDir


    @TaskAction
    void generateFile() {

        File gitDir = project.file(gitDir)
        File output = project.file(destinationFile)
        String version = project.version

        logger.quiet "Writing git version for git repo: '$gitDir', to: '$output' (project version: '$version')"

        new VersionFileGenerator().execute(gitDir, output, version)
    }
}
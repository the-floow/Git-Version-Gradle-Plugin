package com.thefloow.gradle.gitversion.gradle

import com.thefloow.gradle.gitversion.VersionFileGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GeneratePropertyFileTask extends DefaultTask {

    def destinationFile
    def gitDir


    @TaskAction
    void generateFile() {

        File genDir = new File(project.buildDir, "generated-version-info")
        File outputFile = new File(genDir, destinationFile().get().path)

        File gitDir = project.file(gitDir)
        String version = project.version

        project.sourceSets.main.output.dir(genDir, builtBy: name)

        logger.quiet "Writing git version for git repo: '$gitDir', to: '$outputFile' (project version: '$version')"

        new VersionFileGenerator().execute(gitDir, outputFile, version)
    }
}
package com.beust.kobalt.plugin.retrolambda

import com.beust.kobalt.TaskResult
import com.beust.kobalt.api.*
import com.beust.kobalt.api.annotation.Directive
import com.beust.kobalt.misc.KobaltLogger
import com.beust.kobalt.misc.log
import com.google.inject.Singleton
import org.jetbrains.dokka.DokkaGenerator
import org.jetbrains.dokka.DokkaLogger
import org.jetbrains.dokka.SourceLinkDefinition
import java.io.File
import java.util.*

@Singleton
class DokkaPlugin : ConfigsPlugin<DokkaConfig>() {

    override val name = PLUGIN_NAME

    companion object {
        const val PLUGIN_NAME = "Dokka"
    }

    fun generateDoc(project: Project, context: KobaltContext, info: CompilerActionInfo) : TaskResult {
        val configs = configurationFor(project)
        val classpath = context.dependencyManager.calculateDependencies(project, context)
        val buildDir = project.buildDirectory
        val classpathList = classpath.map { it.jarFile.get().absolutePath } + listOf(buildDir)
        var success = true
        configs.forEach { config ->
            if (!config.skip) {
                val outputDir = buildDir + "/" +
                        if (config.outputDir.isBlank()) "doc" else config.outputDir

                val gen = DokkaGenerator(
                        KobaltDokkaLogger { success = false },
                        classpathList,
                        project.sourceDirectories.filter { File(it).exists() }.toList(),
                        config.samplesDirs,
                        config.includeDirs,
                        config.moduleName,
                        outputDir,
                        config.outputFormat,
                        config.sourceLinks.map { SourceLinkDefinition(it.dir, it.url, it.urlSuffix) }
                )
                gen.generate()
                log(2, "Documentation generated in $outputDir")
            } else {
                log(2, "skip is true, not generating the documentation")
            }
        }
        return TaskResult(success)
    }

}

@Directive
public fun Project.dokka(init: DokkaConfig.() -> Unit) = let { project ->
    with(DokkaConfig()) {
        init()
        (Kobalt.findPlugin(DokkaPlugin.PLUGIN_NAME) as DokkaPlugin).addConfiguration(project, this)
    }
}

class SourceLinkMapItem {
    var dir: String = ""
    var url: String = ""
    var urlSuffix: String? = null
}

class DokkaConfig(
        var samplesDirs: List<String> = emptyList(),
        var includeDirs: List<String> = emptyList(),
        var outputDir: String = "",
        var outputFormat: String = "html",
        var sourceLinks : ArrayList<SourceLinkMapItem> = arrayListOf<SourceLinkMapItem>(),
        var moduleName: String = "",
        var skip: Boolean = false) {

    fun sourceLinks(init: SourceLinkMapItem.() -> Unit)
            = sourceLinks.add(SourceLinkMapItem().apply { init() })
}

class KobaltDokkaLogger(val onErrorCallback: () -> Unit = {}) : DokkaLogger {
    override fun error(message: String) {
        KobaltLogger.logger.error("Dokka", message)
        onErrorCallback()
    }

    override fun info(message: String) {
        KobaltLogger.logger.log(2, message)
    }

    override fun warn(message: String) {
        KobaltLogger.logger.warn("Dokka", message)
    }
}

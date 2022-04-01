package org.jetbrains.reflekt.plugin.ic

import org.jetbrains.kotlin.cli.common.ExitCode
import java.io.File

data class TestCompilationResult(
    val exitCode: ExitCode,
    val compileErrors: Collection<String>,
    val compiledFiles: Set<File>
) {
    constructor(
        icReporter: TestICReporter,
        messageCollector: TestMessageCollector
    ) : this(icReporter.exitCode, messageCollector.errors, icReporter.compiledFiles)
}

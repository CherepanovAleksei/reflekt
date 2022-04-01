package org.jetbrains.reflekt.plugin.ic

import org.jetbrains.reflekt.util.file.getAllNestedFiles
import org.jetbrains.kotlin.cli.common.arguments.parseCommandLineArguments
import org.jetbrains.reflekt.plugin.analysis.getTestsDirectories
import org.jetbrains.reflekt.plugin.ic.modification.Modification
import org.jetbrains.reflekt.plugin.util.Util
import org.jetbrains.reflekt.plugin.util.Util.clear
import org.jetbrains.reflekt.plugin.util.Util.getTempPath
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class IncrementalCompilationTest {
    // File with compiler arguments (see K2JVMCompilerArguments)
    // If we would like to add additional arguments in tests we can use this file
    private val argumentsFileName = "args.txt"

    // The name of file with the main function
    private val mainFileName = "Main"
    private val outFolderName = "out"

    companion object {
        @JvmStatic
        fun data(): List<Arguments> {
            return getTestsDirectories(IncrementalCompilationTest::class).map { directory ->
                // TODO: get modifications for each directory (maybe deserialize it?)
                Arguments.of(directory, emptyList<Modification>(), null)
            }
        }
    }

    @Tag("ic")
    @MethodSource("data")
    @ParameterizedTest(name = "test {index}")
    fun incrementalCompilationTest(sourcesPath: File, modifications: List<Modification>, expectedResult: String?) {
        val testRoot = initTestRoot()
        val srcDir = File(testRoot, "src").apply { mkdirs() }
        val cacheDir = File(testRoot, "incremental-data").apply { mkdirs() }
        val outDir = File(testRoot, outFolderName).apply { mkdirs() }
        val srcRoots = listOf(srcDir)

        sourcesPath.copyRecursively(srcDir, overwrite = true)
        val testDataPath = File(Util.getResourcesRootPath(IncrementalCompilationTest::class))
        val pathToDownloadKotlinSources = File(testDataPath.parent, "kotlinSources").apply { mkdirs() }
        val compilerArgs = createCompilerArguments(outDir, srcDir, pathToDownloadKotlinSources).apply {
            parseCommandLineArguments(parseAdditionalCompilerArgs(srcDir, argumentsFileName), this)
            pluginClasspaths = arrayOf(
                File("build/libs").listFiles()?.singleOrNull { it.name.matches(Regex("reflekt-plugin-\\d\\.\\d{1,2}\\.\\d{1,3}\\.jar")) }?.absolutePath
                    ?: fail("reflekt-plugin jar was not built for testing")
            )
        }

        val compiledFilesInitial = compileSources(cacheDir, srcRoots, compilerArgs, "Initial")
        assert(compiledFilesInitial == setOf("definitions.kt", "dummy.kt", "Main.kt"))

        val newFile = File(srcDir, "new.kt")
        newFile.writeText(
            """
            package org.jetbrains.reflekt.plugin.ic.data.base_test
            
            @A
            object D:B""".trimIndent()
        )

        val incCompiledFiles = compileSources(cacheDir, srcRoots, compilerArgs, "Modified")
        assert(incCompiledFiles == setOf("definitions.kt", "Main.kt", newFile.name))

        val actualResult = runCompiledCode(outDir, compilerArgs.classpath)

        // Compare the initial result and result without IC
        cacheDir.clear()
        val compiledFilesRebuild = compileSources(cacheDir, srcRoots, compilerArgs, "Without IC")
        assert(compiledFilesRebuild == setOf("definitions.kt", "dummy.kt", "Main.kt", newFile.name))

        val actualResultWithoutIC = runCompiledCode(outDir, compilerArgs.classpath)
        Assertions.assertEquals(actualResult, actualResultWithoutIC, "The initial result and result after IC are different!")

        testRoot.deleteRecursively()
    }

    private fun runCompiledCode(outDir: File, classpath: String? = null): String {
        val classpathProperty = if (classpath != null) listOf("-classpath", "${outDir.absolutePath}:$classpath") else listOf()
        val commands = listOf("java") + classpathProperty + listOf(getMainClass(outDir))
        return Util.runProcessBuilder(
            Util.Command(commands, directory = outDir.absolutePath)
        )
    }

    // Find [mainFileName]Kt.class file in [outDir] and make the following transformations:
    //  - сut <class> extension
    //  - get the relative path with [outDir]
    //  - replace all "/" into "."
    private fun getMainClass(outDir: File): String {
        val allFiles = outDir.absolutePath.getAllNestedFiles()
        val mainClass = allFiles.find { it.name == "${mainFileName}Kt.class" }?.absolutePath?.removeSuffix(".class")
            ?: error("The output directory doe not contains ${mainFileName}Kt.class file")
        return mainClass.substring(mainClass.indexOf("$outFolderName/") + outFolderName.length + 1).replace("/", ".")
    }

    // If we had failed tests the previous results were not deleted and it can throw some compiler errors
    private fun initTestRoot(): File {
        val testRoot = File(getTempPath(), IncrementalCompilationTest::class.java.simpleName)
        if (testRoot.exists()) {
            testRoot.deleteRecursively()
        }
        testRoot.apply { mkdirs() }
        return testRoot
    }
}

package space.kiibou

import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import java.io.File
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.Paths
import javax.tools.StandardLocation

class TestRunner(private val watchPath: Path) {
    private val fileWatcher = FileWatcher()
    private val compiler = Compiler()

    fun run() {
        fileWatcher.onFileChange(watchPath, onModify = ::fileChanged)
    }

    private fun fileChanged(file: Path) {
        val javaFile = JavaFile(file)

        if (compiler.compile(javaFile)) {
            val fileName = javaFile.className
                ?: throw IllegalStateException("Error processing file $file! Could not find class name.")
            val packageName = javaFile.pkg
            val fqn = javaFile.fqn!!

            val fileObject =
                compiler.fileManager.getFileForOutput(StandardLocation.CLASS_OUTPUT, packageName, fileName, null)

            val compiledFile = File(fileObject.toUri().toString().removePrefix("file:/") + ".class")

            println("Compiled $compiledFile")

            val cls = classFromBytes<Any>(fqn, compiledFile.readBytes())

            if (cls != null) {
                executeTests(cls)
            } else {
                println("Error loading class!")
            }
        }
    }

    private fun executeTests(cls: Class<out Any>?) {
        val listener = SummaryGeneratingListener()

        val request = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClass(cls))
            .build()
        val launcher = LauncherFactory.create()
        val testPlan = launcher.discover(request)
        launcher.registerTestExecutionListeners(listener)
        launcher.execute(request)

        listener.summary.printTo(PrintWriter(System.out))

        listener.summary.failures.forEach {
            println("Test ${it.testIdentifier.displayName} in ${(it.testIdentifier.source.get() as MethodSource).className} failed with exception:")
            it.exception.printStackTrace()
        }
    }
}

fun main(args: Array<String>) {
    val testRunner = TestRunner(Paths.get(args[0]))

    testRunner.run()
}

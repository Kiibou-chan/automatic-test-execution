package space.kiibou

import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.Paths

class TestRunner(private val watchPath: Path) {
    private val fileWatcher = FileWatcher()
    private val compiler = Compiler()

    fun run() {
        fileWatcher.onFileChange(watchPath, onModify = ::fileChanged)
    }

    private fun fileChanged(file: Path) {
        val javaFile = JavaFile(file)

        val bytecode = compiler.compile(javaFile)

        val cls = classFromBytes<Any>(javaFile.fqn!!, bytecode)
            ?: throw IllegalStateException("Error loading bytecode for class $file")

        executeTests(cls)
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

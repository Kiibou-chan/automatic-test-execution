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
import javax.tools.JavaFileObject
import javax.tools.StandardLocation
import kotlin.io.path.name
import kotlin.io.path.readLines

class TestRunner(private val watchPath: Path) {
    private val compiler = Compiler()

    fun run() {
        val hashCodes: MutableMap<Path, Int> = mutableMapOf()

        println(System.getProperty("java.class.path").split(File.pathSeparator))

        watchFile(watchPath) {
            val hashCode = it.readLines().hashCode()

            if (!hashCodes.containsKey(it) || hashCodes.containsKey(it) && hashCodes[it] != hashCode) {
                try {
                    fileChanged(it)
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }

                hashCodes[it] = hashCode
            }
        }
    }

    private fun fileChanged(file: Path) {
        if (compiler.compile(JavaFile(file.toUri(), JavaFileObject.Kind.SOURCE))) {
            val fileName = file.name.split(".")[0]
            val fileObject =
                compiler.fileManager.getFileForOutput(StandardLocation.CLASS_OUTPUT, "space.kiibou", fileName, null)

            val compiledFile = File(fileObject.toUri().toString().removePrefix("file:/") + ".class")

            println("Compiled $compiledFile")

            val cls = classFromBytes<Any>("space.kiibou.$fileName", compiledFile.readBytes())

            if (cls != null) {
                /*
                val obj = cls.getConstructor().newInstance()

                cls.methods.forEach { method ->
                    if (method.name.contains("test") && method.parameterCount == 0) {
                        println("Found Method $method")
                        try {
                            method.invoke(obj)
                        } catch (ex: InvocationTargetException) {
                            ex.targetException.printStackTrace()
                        } catch (ex: Throwable) {
                            ex.printStackTrace()
                        }
                    }
                }
                 */

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
            } else {
                println("Error loading class!")
            }
        }
    }
}

fun main() {
    val testRunner = TestRunner(Paths.get("src/test/kotlin/space/kiibou"))

    testRunner.run()
}
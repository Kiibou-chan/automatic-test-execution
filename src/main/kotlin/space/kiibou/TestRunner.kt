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
        val javaFile = JavaFile(file)

        if (compiler.compile(javaFile)) {
            val fileName = javaFile.getClassName() ?: throw IllegalStateException("Error processing file $file! Could not find class name.")
            val packageName = javaFile.getPackage()

            val fqn = if (packageName.isEmpty()) {
                fileName
            } else {
                "${packageName}.$fileName"
            }

            val fileObject =
                compiler.fileManager.getFileForOutput(StandardLocation.CLASS_OUTPUT, packageName, fileName, null)

            val compiledFile = File(fileObject.toUri().toString().removePrefix("file:/") + ".class")

            println("Compiled $compiledFile")

            val cls = classFromBytes<Any>(fqn, compiledFile.readBytes())

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

fun main(args: Array<String>) {
    val testRunner = TestRunner(Paths.get(args[0]))

    testRunner.run()
}

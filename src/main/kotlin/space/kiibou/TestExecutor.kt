package space.kiibou

import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.analysis.ICounter
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.core.data.SessionInfoStore
import org.jacoco.core.instr.Instrumenter
import org.jacoco.core.runtime.IRuntime
import org.jacoco.core.runtime.LoggerRuntime
import org.jacoco.core.runtime.RuntimeData
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import kotlin.math.roundToInt


class TestExecutor {
    private val userClasses: MutableList<Class<out Any>> = mutableListOf()
    private val testClasses: MutableList<Class<out Any>> = mutableListOf()

    private val listener = SummaryGeneratingListener()

    private val classLoader = InMemoryClassLoader()

    // coverage
    private val runtime: IRuntime = LoggerRuntime()
    private val instrumenter: Instrumenter = Instrumenter(runtime)
    private val runtimeData = RuntimeData()

    private val executionData = ExecutionDataStore()
    private val sessionInfos = SessionInfoStore()

    fun addTestClass(file: JavaFile, bytecode: ByteArray) {
        classLoader.addClassDefinition(file, bytecode)

        testClasses += classLoader.loadClass(file.fqn)
    }

    fun addUserClass(cls: Class<out Any>) {
        classLoader.addClassDefinition(cls.name, instrumenter.instrument(getUserClassByteCode(cls), cls.name))

        userClasses += classLoader.loadClass(cls.name)
    }

    fun executeTests() {
        runtime.startup(runtimeData)

        val request = LauncherDiscoveryRequestBuilder.request()
            .selectors(testClasses.map(DiscoverySelectors::selectClass))
            .build()

        val launcher = LauncherFactory.create()

        @Suppress("UNUSED_VARIABLE")
        val testPlan = launcher.discover(request)

        launcher.registerTestExecutionListeners(listener)
        launcher.execute(request)

        runtimeData.collect(executionData, sessionInfos, false)

        runtime.shutdown()
    }

    fun printSummary(out: OutputStream) {
        fun printCounter(counter: ICounter, unit: String) {
            if (!counter.missedRatio.isNaN()) {
                println("${counter.missedCount} of ${counter.totalCount} (${(counter.missedRatio * 100).roundToInt()}%) $unit missed")
            }
        }

        val writer = PrintWriter(out)

        listener.summary.printTo(writer)

        listener.summary.failures.forEach {
            writer.println("Test ${it.testIdentifier.displayName} in ${(it.testIdentifier.source.get() as MethodSource).className} failed with exception:")
            it.exception.printStackTrace(writer)
        }

        println()

        val coverageBuilder = CoverageBuilder()
        val analyzer = Analyzer(executionData, coverageBuilder)

        userClasses.forEach {
            getUserClassByteCode(it).use { stream ->
                analyzer.analyzeClass(stream, it.name)
            }
        }

        coverageBuilder.classes.forEach { coverage ->
            println("Coverage of class ${coverage.name}\n")

            printCounter(coverage.instructionCounter, "instructions")
            printCounter(coverage.branchCounter, "branches")
            printCounter(coverage.lineCounter, "lines")
            printCounter(coverage.methodCounter, "methods")
            printCounter(coverage.complexityCounter, "complexity")

            println("\nLines:")
            for (i in coverage.firstLine..coverage.lastLine) {
                when (coverage.getLine(i).status) {
                    ICounter.NOT_COVERED -> println("Line $i: RED")
                    ICounter.PARTLY_COVERED -> println("Line $i: YELLOW")
                    ICounter.FULLY_COVERED -> println("Line $i: GREEN")
                }
            }
        }
    }

    private fun getUserClassByteCode(cls: Class<out Any>): InputStream {
        return this::class.java.classLoader.getResourceAsStream("${cls.name.replace('.', '/')}.class")
            ?: throw IllegalStateException("Failed to load bytecode of class ${cls.name}")
    }

    class InMemoryClassLoader : ClassLoader() {
        private val classDefinitions: MutableMap<String, ByteArray> = mutableMapOf()

        fun addClassDefinition(name: String, bytecode: ByteArray) {
            classDefinitions[name] = bytecode
        }

        fun addClassDefinition(file: JavaFile, bytecode: ByteArray) {
            classDefinitions[file.fqn] = bytecode
        }

        override fun loadClass(name: String, resolve: Boolean): Class<*> {
            val bytes: ByteArray? = classDefinitions[name]

            return if (bytes != null) {
                defineClass(name, bytes, 0, bytes.size)
            } else {
                super.loadClass(name, resolve)
            }
        }
    }

}
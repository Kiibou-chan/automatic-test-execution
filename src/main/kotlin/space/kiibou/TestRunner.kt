package space.kiibou

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

        val executor = TestExecutor()

        executor.addUserClass(ClassUnderTest::class.java)
        executor.addTestClass(javaFile, bytecode)

        executor.executeTests()
        executor.printSummary(System.out)
    }

}

fun main(args: Array<String>) {
    val testRunner = TestRunner(Paths.get(args[0]))

    testRunner.run()
}

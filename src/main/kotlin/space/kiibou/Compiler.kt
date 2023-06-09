package space.kiibou

import java.io.File
import java.nio.charset.Charset
import java.util.*
import javax.tools.*
import kotlin.io.path.toPath

class Compiler {
    val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()
    val diagnostics = DiagnosticCollector<JavaFileObject>()

    val fileManager: StandardJavaFileManager =
        compiler.getStandardFileManager(diagnostics, Locale.GERMANY, Charset.defaultCharset()).apply {
            setLocation(StandardLocation.CLASS_OUTPUT, listOf(File("out")))
            setLocation(
                StandardLocation.CLASS_PATH,
                System.getProperty("java.class.path").split(File.pathSeparator).map(::File)
            )
        }

    fun compile(file: JavaFile): ByteArray {
        println("Compiling $file")

        val task = compiler.getTask(System.err.writer(), fileManager, diagnostics, null, null, listOf(file))

        for (diagnostic in diagnostics.diagnostics) {
            if (diagnostic.kind == Diagnostic.Kind.ERROR) {
                println(diagnostic.code)
                println(diagnostic.kind)
                println(diagnostic.position)
                println(diagnostic.startPosition)
                println(diagnostic.endPosition)
                println(diagnostic.source)
                println(diagnostic.getMessage(null))
            }
        }

        val res = task.call()

        if (!res) throw CompilerException(file)

        fileManager.flush()

        return getCompiledBytecode(file)
    }

    private fun getCompiledBytecode(file: JavaFile): ByteArray {
        val fileObject =
            fileManager.getFileForOutput(StandardLocation.CLASS_OUTPUT, file.pkg, file.className, null)

        val compiledFile = File(fileObject.toUri().toPath().toString() + ".class")

        return compiledFile.readBytes()
    }

}

package space.kiibou

import java.net.URI
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.regex.Pattern
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import kotlin.io.path.readLines
import kotlin.io.path.toPath

class JavaFile(uri: URI) : SimpleJavaFileObject(uri, JavaFileObject.Kind.SOURCE) {
    constructor(path: Path) : this(path.toUri())

    private val content: String = uri.toPath().readLines(Charset.defaultCharset()).joinToString("\n")

    val pkg: String
        get() {
            val matcher = packagePattern.matcher(content)

            if (!matcher.find()) return ""

            return matcher.group("name")
        }

    val className: String
        get() {
            val matcher = classNamePattern.matcher(content)

            if (!matcher.find()) throw IllegalStateException("Error parsing file ${this.uri}. Could not find class name.")

            return matcher.group("name")
        }

    val fqn: String
        get() {
            val name = className
            val pkg = this.pkg

            return if (pkg.isEmpty()) name
            else "$pkg.$name"
        }

    override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
        return content
    }

    override fun toString(): String {
        return "JavaFile($fqn)"
    }

    companion object {
        private val namePattern: Pattern =
            Pattern.compile("(?<name>[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)")
        private val packagePattern: Pattern = Pattern.compile("package +$namePattern *;")
        private val classNamePattern: Pattern = Pattern.compile("class +$namePattern *\\{")
    }
}
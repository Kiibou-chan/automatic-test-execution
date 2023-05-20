package space.kiibou

import java.net.URI
import java.nio.charset.Charset
import java.util.regex.Pattern
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import kotlin.io.path.readLines
import kotlin.io.path.toPath

class JavaFile(uri: URI) : SimpleJavaFileObject(uri, JavaFileObject.Kind.SOURCE) {

    private val content: String = uri.toPath().readLines(Charset.defaultCharset()).joinToString("\n")

    fun getPackage(): String {
        val matcher = packagePattern.matcher(content)

        if (!matcher.find()) return ""

        return matcher.group("name")
    }

    fun getClassName(): String? {
        val matcher = classNamePattern.matcher(content)

        if (!matcher.find()) return null

        return matcher.group("name")
    }

    override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
        return content
    }

    companion object {
        private val namePattern: Pattern = Pattern.compile("(?<name>[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)")
        private val packagePattern: Pattern = Pattern.compile("package +$namePattern *;")
        private val classNamePattern: Pattern = Pattern.compile("class +$namePattern *\\{")
    }
}
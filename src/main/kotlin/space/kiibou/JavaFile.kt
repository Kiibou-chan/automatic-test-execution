package space.kiibou

import java.net.URI
import java.nio.charset.Charset
import java.util.regex.Matcher
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

    override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
        return content
    }

    companion object {
        val namePattern = Pattern.compile("(?<name>[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)")
        val packagePattern = Pattern.compile("package $namePattern;")
    }
}
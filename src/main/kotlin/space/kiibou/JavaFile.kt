package space.kiibou

import java.net.URI
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import kotlin.io.path.readLines
import kotlin.io.path.toPath

class JavaFile(uri: URI, kind: JavaFileObject.Kind) : SimpleJavaFileObject(uri, kind) {
    override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
        return uri.toPath().readLines().joinToString("\n")
    }
}
package space.kiibou

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.net.URI
import java.nio.file.Paths

class JavaFileTest {

    private fun getTestFile(name: String): URI = Paths.get("src", "test", "resources", name).toUri()

    @Test
    fun testGetPackage() {
        val file = JavaFile(getTestFile("TestClass.java"))

        assertEquals("test._1", file.getPackage())
    }

}
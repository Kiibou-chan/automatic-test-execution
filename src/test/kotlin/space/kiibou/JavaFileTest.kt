package space.kiibou

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.nio.file.Paths

class JavaFileTest {

    private fun getJavaFile(name: String): JavaFile = JavaFile(Paths.get("src", "test", "resources", name).toUri())

    @Test
    fun testGetCharContents() {
        val file = getJavaFile("TestClass.java")

        assertEquals("""
            package test._1;

            class TestClass {

            }
        """.trimIndent(), file.getCharContent(true))
    }

    @Test
    fun testGetPackage() {
        val file = getJavaFile("TestClass.java")

        assertEquals("test._1", file.pkg)
    }

    @Test
    fun testNoPackage() {
        val file = getJavaFile("EmptyFile.java")

        assertEquals("", file.pkg)
    }

    @Test
    fun testGetName() {
        val file = getJavaFile("TestClass.java")

        assertEquals("TestClass", file.className)
    }

    @Test
    fun testNoName() {
        val file = getJavaFile("EmptyFile.java")

        assertNull(file.className)
    }

    @Test
    fun testFQN() {
        val file = getJavaFile("TestClass.java")

        assertEquals("test._1.TestClass", file.fqn)
    }

    @Test
    fun testNoFQN() {
        val file = getJavaFile("EmptyFile.java")

        assertNull(file.fqn)
    }

}
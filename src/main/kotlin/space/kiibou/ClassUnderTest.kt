package space.kiibou

class ClassUnderTest {

    fun <T> identity(t: T): T = t

    fun <T> wrongIdentity(t: T): T? = null

}

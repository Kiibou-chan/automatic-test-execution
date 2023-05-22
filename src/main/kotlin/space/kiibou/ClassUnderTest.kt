package space.kiibou

class ClassUnderTest {

    fun <T> identity(t: T): T = t

    fun <T> wrongIdentity(t: T): T? = null

    fun conditionalFun(value: Int): String = when {
        value < 0 -> "Negative"
        value < 10 -> "Small"
        else -> "Big"
    }

}

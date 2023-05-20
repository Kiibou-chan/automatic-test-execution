package space.kiibou


inline fun <reified T> classFromBytes(name: String, bytecode: ByteArray): Class<out T>? {
    return object : ClassLoader(JavaFile::class.java.classLoader) {
        val c = defineClass(name, bytecode, 0, bytecode.size).asSubclass(T::class.java)
    }.c
}
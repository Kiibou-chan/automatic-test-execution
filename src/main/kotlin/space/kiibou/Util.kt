package space.kiibou

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.exists


fun watchFile(path: Path, fileTypes: List<String> = listOf("java"), onModify: (Path) -> Unit) {
    val watchService = FileSystems.getDefault().newWatchService()

    path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)

    var poll = true

    while (poll) {
        val key = watchService.take()

        for (event in key.pollEvents()) {
            if (event.context() !is Path) continue

            val eventPath = event.context() as Path

            if (eventPath.toString().split(".").last() in fileTypes) {
                val filePath = path.resolve(eventPath)

                if (filePath.exists()) {
                    onModify(path.resolve(eventPath))
                }
            }
        }

        poll = key.reset()
    }
}

inline fun <reified T> classFromBytes(name: String, bytecode: ByteArray): Class<out T>? {
    return object : ClassLoader(JavaFile::class.java.classLoader) {
        val c = defineClass(name, bytecode, 0, bytecode.size).asSubclass(T::class.java)
    }.c
}
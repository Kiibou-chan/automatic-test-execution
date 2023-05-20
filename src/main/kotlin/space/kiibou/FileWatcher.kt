package space.kiibou

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.exists
import kotlin.io.path.readLines

class FileWatcher {
    private val hashCodes: MutableMap<Path, Int> = mutableMapOf()

    fun onFileChange(path: Path, fileTypes: List<String> = listOf("java"), onModify: (Path) -> Unit) {
        watchFile(path, fileTypes) {
            val hashCode = it.readLines().hashCode()

            if (!hashCodes.containsKey(it) || hashCodes.containsKey(it) && hashCodes[it] != hashCode) {
                try {
                    onModify(it)
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }

                hashCodes[it] = hashCode
            }
        }
    }

    companion object {
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

    }

}
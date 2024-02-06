package cc.originx.lrd1122ss.ServerManager

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

class Utils {
    companion object {
        @Throws(IOException::class)
        fun copyFolder(source: Path, target: Path) {
            Files.walk(source).use { stream ->
                stream.collect(Collectors.toList()).forEach { s ->
                    try {
                        val t = target.resolve(source.relativize(s))
                        if (Files.isDirectory(s)) {
                            if (!Files.exists(t)) {
                                Files.createDirectory(t)
                            }
                        } else {
                            Files.copy(s, t)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
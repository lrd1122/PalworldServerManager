package cc.originx.lrd1122ss.ServerManager

import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class ConfigManager(private val fileName: String){


    private val jarPath = ConfigManager::class.java.protectionDomain.codeSource.location.toURI().path
    private val jarDirectory = File(jarPath).parent
    private val filePath: Path = Paths.get(jarDirectory, fileName)

    init {
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun readProperty(key: String): String? {
        val properties = Properties()
        try {
            BufferedReader(Files.newBufferedReader(filePath)).use { reader ->
                properties.load(reader)
            }
            return properties.getProperty(key)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun writeProperty(key: String, value: String) {
        val properties = Properties()
        try {
            BufferedReader(Files.newBufferedReader(filePath)).use { reader ->
                properties.load(reader)
            }

            properties.setProperty(key, value)

            BufferedWriter(Files.newBufferedWriter(filePath)).use { writer ->
                properties.store(writer, null)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
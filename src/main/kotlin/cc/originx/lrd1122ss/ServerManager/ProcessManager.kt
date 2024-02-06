package cc.originx.lrd1122ss.ServerManager

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class ProcessManager {
    companion object {
        fun getProcessIdByTasklist(processName: String): Int {
            val processBuilder = ProcessBuilder("tasklist", "/FI", "IMAGENAME eq $processName", "/FO", "CSV")
            val process = processBuilder.start()
            process.waitFor()
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line!!.contains(processName)) {
                        val parts = line!!.split(",".toRegex()).toTypedArray()
                        var pidString = parts[1].trim { it <= ' ' }.replace("\"", "") // Remove extra quotes if present
                        return pidString.toInt()
                    }
                }
            }
            return -1
        }
    }
}
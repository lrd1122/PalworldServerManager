package cc.originx.lrd1122ss.ServerManager

import java.io.BufferedReader
import java.io.InputStreamReader

class MemoryManager {
    companion object {
        fun getMemoryUsageByPid(pid: Int): Long {
            val command = "wmic process where ProcessID=$pid get WorkingSetSize /format:list"
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line!!.contains("WorkingSetSize")) {
                        val memoryString = line!!.split("=")[1].trim()
                        val memoryInKB = memoryString.toLong() / 1024 // Convert bytes to KB
                        return memoryInKB / 1024 // Convert KB to MB
                    }
                }
            }

            return 0
        }
        fun getSystemAvailableMemory(): Long {
            val process = Runtime.getRuntime().exec("wmic OS get FreePhysicalMemory /Value")
            process.waitFor()

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                var freeMemory: Long = 0

                while (reader.readLine().also { line = it } != null) {
                    if (line?.contains("FreePhysicalMemory") == true) {
                        freeMemory = line!!.split("=")[1].trim().toLong()
                        break
                    }
                }

                return freeMemory / 1024
            }
        }
    }
}
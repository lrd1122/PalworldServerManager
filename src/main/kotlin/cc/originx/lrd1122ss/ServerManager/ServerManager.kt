package cc.originx.lrd1122ss.ServerManager

import cc.originx.lrd1122ss.ServerController
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class ServerManager {
    companion object {
        fun startServer() {
            ServerController.gameThread = Thread(Runnable {
                val processBuilder = ProcessBuilder(ServerController.palServerShippingPath.path)
                processBuilder.redirectErrorStream(true)
                val process = processBuilder.start()
                ServerController.gameProcess = process
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (process.isAlive) {
                    while (reader.readLine().also { line = it } != null) {
                        println("外部程序输出: $line")
                    }
                }
            })
            ServerController.gameThread?.start()
        }
        fun stopServer(){
            ServerController.gameProcess?.destroy()
            ServerController.gameThread?.interrupt()
            if (ServerController.gameProcess == null || ServerController.gameThread == null) {
                val processHandle: Optional<ProcessHandle> = ProcessHandle.of(ServerController.palServerProcessID.toLong())
                val process = processHandle.get()
                process.destroy()
            }
        }
    }
}
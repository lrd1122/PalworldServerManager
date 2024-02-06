package cc.originx.lrd1122ss

import cc.originx.lrd1122ss.ServerManager.ConfigManager
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class PalworldServerManager : Application() {
    companion object{
        var configManager: ConfigManager? = null
    }
    override fun start(stage: Stage) {
        configManager = ConfigManager("config.properties")

        //主界面
        val fxmlLoader = FXMLLoader(PalworldServerManager::class.java.getResource("mainWindow.fxml"))
        val scene = Scene(fxmlLoader.load())
        stage.title = "PalworldServerManager"
        stage.scene = scene
        stage.isResizable = false
        stage.show()
    }

    override fun stop() {
        ServerController.gameProcess?.destroy()
        ServerController.gameThread?.interrupt()
    }
}

fun main() {
    Application.launch(PalworldServerManager::class.java)
}
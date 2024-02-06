package cc.originx.lrd1122ss

import cc.originx.lrd1122ss.ServerManager.*
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.layout.Pane
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.util.Duration
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class ServerController {
    companion object {
        val processName = "PalServer-Win64-Test-Cmd.exe"
        var palServerProcessID = -1

        var palServerDir = File("")
        var palServerPath = File("")
        var palServerShippingPath = File("")
        var palServerConfigPath = File("")
        var palServerSaveDir = File("")

        var gameThread: Thread? = null
        var gameProcess: Process? = null
        var gameMemoryUsed: Long = 0
        var systemMemoryLeft: Long = 0

        val jarPath = ConfigManager::class.java.protectionDomain.codeSource.location.toURI().path
        val jarDirectory = File(jarPath).parent
        val backupDir = File(jarDirectory, "\\SavesBackup")
    }

    @FXML
    private lateinit var exePathText: Text

    @FXML
    private lateinit var processIDText: Text

    @FXML
    private lateinit var serverStateText: Text

    @FXML
    private lateinit var gameMemoryUsedText: Text

    @FXML
    private lateinit var switchServerStateButton: Button

    @FXML
    private lateinit var mainWindow: Pane

    @FXML
    private lateinit var memoryLimitCheckBox: CheckBox

    @FXML
    private lateinit var memoryLimitTextField: TextField

    @FXML
    private lateinit var systemMemoryUsedCheckBox: CheckBox

    @FXML
    private lateinit var systemMemoryLeftText: Text

    @FXML
    private lateinit var systemMemoryUsedTextField: TextField
    @FXML
    private lateinit var backupChoiceBox: ChoiceBox<String>
    @FXML
    private lateinit var autoBackupIntervalTextField: TextField
    @FXML
    private lateinit var autoBackupCheckBox: CheckBox
    @FXML
    private lateinit var openBackupFolderButton: Button

    private val timeline = Timeline()
    private var runTime = 0

    @FXML
    fun initialize() {
        //初始化
        PalworldServerManager.configManager?.readProperty("palServerPath").let {
            if (it != null) {
                palServerPath = File(it.toString())
                exePathText.text = it.toString()
            }
        }
        val memoryLimitTooltip = Tooltip("当PalServer内存占用超过阈值时，将会自动重启服务器")
        memoryLimitTooltip.font = Font("Arial", 14.0)
        memoryLimitTooltip.showDelay = Duration(0.0)
        memoryLimitTooltip.hideDelay = Duration(0.0)
        memoryLimitCheckBox.tooltip = memoryLimitTooltip

        val systemMemoryUsedTooltip = Tooltip("当系统可用内存小于阈值时，将会自动重启服务器")
        systemMemoryUsedTooltip.font = Font("Arial", 14.0)
        systemMemoryUsedTooltip.showDelay = Duration(0.0)
        systemMemoryUsedTooltip.hideDelay = Duration(0.0)
        systemMemoryUsedCheckBox.tooltip = systemMemoryUsedTooltip


         memoryLimitTextField.textProperty().addListener { _, _, _ ->
             if(memoryLimitTextField.text.length > 0 && memoryLimitTextField.text.matches(Regex("\\d*"))) {
                     ManagerSettings.maxinumMemory = memoryLimitTextField.text.toInt()
                 } else{
                 ManagerSettings.maxinumMemory = 8192
             }
         }
        systemMemoryUsedTextField.textProperty().addListener { _, _, _ ->
            if(systemMemoryUsedTextField.text.length > 0 && systemMemoryUsedTextField.text.matches(Regex("\\d*"))) {
                ManagerSettings.systemMininumMemory = systemMemoryUsedTextField.text.toInt()
            } else{
                ManagerSettings.systemMininumMemory = 1024
            }
        }
        autoBackupIntervalTextField.textProperty().addListener { _, _, _ ->
            if(autoBackupIntervalTextField.text.length > 0 && autoBackupIntervalTextField.text.matches(Regex("\\d*"))) {
                ManagerSettings.autoBackupInterval = autoBackupIntervalTextField.text.toInt()
            } else{
                ManagerSettings.autoBackupInterval = 60
            }
        }
        memoryLimitTextField.textProperty().addListener { _, oldValue, newValue ->
            if (!newValue.matches(Regex("\\d*"))) {
                memoryLimitTextField.text = oldValue
            }
        }
        systemMemoryUsedTextField.textProperty().addListener { _, oldValue, newValue ->
            if (!newValue.matches(Regex("\\d*"))) {
                systemMemoryUsedTextField.text = oldValue
            }
        }
        autoBackupIntervalTextField.textProperty().addListener { _, oldValue, newValue ->
            if (!newValue.matches(Regex("\\d*"))) {
                autoBackupIntervalTextField.text = oldValue
            }
        }

        backupChoiceBox.items = FXCollections.observableArrayList( "相对路径" )
        backupChoiceBox.value = backupChoiceBox.items[0]

        val keyFrame = KeyFrame(Duration.seconds(1.0), this::onTimerTick)
        timeline.keyFrames.add(keyFrame)
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()
    }

    private fun onTimerTick(event: ActionEvent) {
        runTime++
        this.updateProcessID()
        this.updateMemoryUsed()

        if(memoryLimitCheckBox.isSelected && palServerProcessID != -1){
            if(gameMemoryUsed > ManagerSettings.maxinumMemory){
                ServerManager.stopServer()
                updateProcessID()
                ServerManager.startServer()
                updateProcessID()
            }
        } else if(systemMemoryUsedCheckBox.isSelected && palServerProcessID != -1){
            if(systemMemoryLeft < ManagerSettings.systemMininumMemory){
                ServerManager.stopServer()
                updateProcessID()
                ServerManager.startServer()
                updateProcessID()
            }
        }

        var runMinute = runTime / 60
        if((runTime % 60) == 0) {
            if (autoBackupCheckBox.isSelected && palServerPath.path != "" && runMinute % ManagerSettings.autoBackupInterval == 0) {
                var date = SimpleDateFormat("yyyy年MM月dd日hh时mm分ss秒").format(Date())
                if (!backupDir.exists()) backupDir.mkdir()
                var savedDir = File(backupDir, "\\$date")
                Utils.copyFolder(palServerSaveDir.toPath(), savedDir.toPath())
            }
        }
    }

    private fun updateProcessID() {
        val processID = ProcessManager.getProcessIdByTasklist(processName)
        palServerProcessID = processID
        processIDText.text = "进程PID: ${palServerProcessID}"
        if (palServerProcessID != -1) {
            serverStateText.text = "运行中"
            switchServerStateButton.text = "关闭"
        } else {
            serverStateText.text = "未运行"
            switchServerStateButton.text = "开启"
        }
    }

    private fun updateMemoryUsed() {
         gameMemoryUsed = MemoryManager.getMemoryUsageByPid(palServerProcessID)
        systemMemoryLeft = MemoryManager.getSystemAvailableMemory()
        gameMemoryUsedText.text = "内存占用: ${gameMemoryUsed}MB/${ManagerSettings.maxinumMemory}MB"
        systemMemoryLeftText.text = "系统剩余内存: ${systemMemoryLeft}MB"
    }

    private fun updateFiles() {
        palServerDir = palServerPath.parentFile
        palServerShippingPath = File(palServerDir, "\\Pal\\Binaries\\Win64\\PalServer-Win64-Test-Cmd.exe")
        palServerConfigPath = File(palServerDir, "\\Pal\\Saved\\Config\\WindowsServer\\PalWorldSettings.ini")
        palServerSaveDir = File(palServerDir, "\\Pal\\Saved\\SaveGames")
    }

    @FXML
    private fun exeChooser_onAction() {
        val fileChooser = FileChooser()
        val extFilter: FileChooser.ExtensionFilter = FileChooser.ExtensionFilter("PalServer.exe", "*.exe")
        fileChooser.getExtensionFilters().add(extFilter)
        var file = fileChooser.showOpenDialog(null)
        if(file != null) {
            exePathText.text = file.path
            palServerPath = file
            PalworldServerManager.configManager?.writeProperty("palServerPath", file.path)
            updateFiles()
        }
    }

    @FXML
    private fun switchServerStateButton_Action() {
        if (palServerProcessID == -1) {
            if (palServerShippingPath.name == "PalServer-Win64-Test-Cmd.exe") {
                ServerManager.startServer()
                updateProcessID()
            }
        } else if (palServerProcessID != -1) {
            ServerManager.stopServer()
            updateProcessID()
        }
    }
    @FXML
    private fun openBackupFolderButton_onAction(){
        if(!backupDir.exists()) backupDir.mkdir()
        Runtime.getRuntime().exec("cmd /c start explorer ${backupDir.path}")
    }
}

package me.sergei4.mobile.tools.mdbgui.ui.controllers.logcat

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.stage.Stage
import javafx.stage.StageStyle
import me.sergei4.mobile.tools.mdbgui.app.context
import me.sergei4.mobile.tools.mdbgui.app.model.MobileDeviceLogLine
import me.sergei4.mobile.tools.mdbgui.extentions.add
import me.sergei4.mobile.tools.mdbgui.ui.dialogs.ProgressDialogController
import me.sergei4.mobile.tools.mdbgui.ui.mainScreen
import java.awt.Desktop
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.function.Predicate

open class LogsController : Initializable {

    @FXML
    private lateinit var btnStartStop: Button

    @FXML
    private lateinit var listViewLog: ListView<MobileDeviceLogLine>

    @FXML
    private lateinit var runnableProcessList: ComboBox<String>

    @FXML
    private lateinit var userFilter: TextField

    private val logItemList = FXCollections.observableArrayList<MobileDeviceLogLine>()
    private val outLogItemList: FilteredList<MobileDeviceLogLine> = FilteredList(logItemList)

    private val processItemList = FXCollections.observableArrayList<String>()
    private val sortedProcessItemList = SortedList(processItemList).apply {
        comparator = Comparator(String::compareTo)
    }

    private val disposables = CompositeDisposable()
    private var running = false

    private class ComplexFilter {
        var processName = ""
        var userFilterStr = ""
        fun createPredicate(): Predicate<MobileDeviceLogLine> {
            val processFilter = Predicate { line: MobileDeviceLogLine ->
                processName == "" || (line.process?.run { simpleName == processName }) ?: false
            }
            val userFilter = Predicate { line: MobileDeviceLogLine ->
                line.toString().toLowerCase().contains(userFilterStr.toLowerCase())
            }
            return processFilter.and(userFilter)
        }
    }

    private val complexFilter = ComplexFilter()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        runnableProcessList.items = sortedProcessItemList
        runnableProcessList.selectionModel.selectedItemProperty()
            .addListener { _, _, newValue ->
                complexFilter.processName = newValue ?: ""
                outLogItemList.predicate = complexFilter.createPredicate()
            }

        listViewLog.items = outLogItemList

        listViewLog.setCellFactory {
            object : ListCell<MobileDeviceLogLine>() {
                override fun updateItem(item: MobileDeviceLogLine?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (!empty && item != null) {
                        text = item.toString()
                        font = context.resources.fonts["courierFont13"]
                    } else {
                        text = null
                    }
                }
            }
        }

        userFilter.textProperty().addListener { _, _, newValue ->
            complexFilter.userFilterStr = newValue ?: ""
            outLogItemList.setPredicate(complexFilter.createPredicate())
        }

        context.model.currentDevice.subscribe {
            logItemList.clear()
            processItemList.clear()
            stop()
        }
    }

    fun init() {

    }

    private fun stop() {
        Platform.runLater { btnStartStop.text = "Start" }
        running = false
        disposables.clear()
    }

    @FXML
    private fun onStartStopClicked() {
        if (running) stop() else start()
    }

    @FXML
    private fun onClearLocallyClicked() {
        logItemList.clear()
    }

    @FXML
    private fun onSaveToFileClicked() {
        val device = context.model.currentDevice.blockingFirst()
        val dialog: Stage = ProgressDialogController.createDialog("Gathering information. Please wait...")
        dialog.initStyle(StageStyle.UNDECORATED)
        mainScreen.showDialog(dialog)
        MobileLogUtils.save(context, device, Observable.fromIterable(outLogItemList)) { Platform.runLater { dialog.close() } }
    }

    @FXML
    private fun onOpenLogFolderClicked() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(context.configuration.logsPath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun start() {
        logItemList.clear()
        processItemList.clear()
        processItemList.add("")
        context.model.observeLog()
            .observeOn(JavaFxScheduler.platform())
            .subscribe(
                { addLine(it) },
                { stop();it.printStackTrace() },
                { stop() }
            ).add(disposables)
        context.model.observeProcesses()
            .flatMap { Observable.fromIterable(it) }
            .filter { process -> !processItemList.contains(process.simpleName) }
            .observeOn(JavaFxScheduler.platform())
            .subscribe(
                { process -> processItemList.add(process.simpleName) },
                { stop() },
                { stop() }
            ).add(disposables)
        btnStartStop.text = "Stop"
        running = true
    }

    private fun addLine(line: MobileDeviceLogLine) {
        logItemList.add(line)
    }
}
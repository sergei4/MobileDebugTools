package me.sergei4.mobile.tools.mdbgui.ui.controllers

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.layout.Pane
import me.sergei4.mobile.tools.mdbgui.app.context
import me.sergei4.mobile.tools.mdbgui.ui.controllers.logcat.LogsController
import me.sergei4.mobile.tools.mdbgui.ui.controllers.screencapture.ScreenCaptureController
import java.net.URL
import java.util.*

class MainController : Initializable {

    @FXML
    private lateinit var workplace: Pane

    @FXML
    private lateinit var btnOpenScreenShotScreen: ToggleButton

    @FXML
    private lateinit var btnOpenLogScreen: ToggleButton

    @FXML
    private lateinit var infoPanel : Pane
    @FXML
    private lateinit var infoPanelText : Label
    @FXML
    private lateinit var infoPanelProgressIndicator : Control

    private val viewsMap = mutableMapOf<Any, Parent>()

    private val screencapture: ScreenCaptureController by lazy {
        val loader = FXMLLoader(javaClass.getResource("/fxml/ScreenCaptureLayout.fxml"))
        val view = loader.load<Parent>()
        val controller: ScreenCaptureController = loader.getController()
        viewsMap[controller] = view
        return@lazy controller
    }

    private val logs: LogsController by lazy {
        val loader = FXMLLoader(javaClass.getResource("/fxml/DeviceLogLayout.fxml"))
        val view = loader.load<Parent>()
        val controller: LogsController = loader.getController()
        viewsMap[controller] = view
        return@lazy controller
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        println("MainController")
        infoPanel.isVisible = false
        screencapture.init()
        logs.init()

        btnOpenScreenShotScreen.selectedProperty().addListener { _, _, _ -> openScreencaptureController() }
        btnOpenLogScreen.selectedProperty().addListener { _, _, _ -> openLogController() }

        btnOpenScreenShotScreen.isSelected = true
    }

    private fun openScreencaptureController() {
        with(workplace.children) {
            removeAll(this)
            add(viewsMap[screencapture])
        }
    }

    private fun openLogController() {
        with(workplace.children) {
            removeAll(this)
            add(viewsMap[logs])
        }
    }

    @FXML
    private fun onOpenLangSettingsClicked() {
        context.model.openLangSettings()
    }

    fun showProgressBottomBar(message: String) {
        infoPanel.isVisible = true
        infoPanelProgressIndicator.isVisible = true
        infoPanelText.text = message
    }

    fun hideBottomBar() {
        infoPanel.isVisible = false
    }
}
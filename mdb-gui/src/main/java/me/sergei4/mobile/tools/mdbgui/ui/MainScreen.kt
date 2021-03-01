package me.sergei4.mobile.tools.mdbgui.ui

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import me.sergei4.mobile.tools.mdbgui.MainGUI
import me.sergei4.mobile.tools.mdbgui.ui.controllers.MainController

class MainScreen(
    private val primaryStage: Stage,
) {
    private val mainController: MainController

    init {
        val loader = FXMLLoader(javaClass.getResource("/fxml/MainController.fxml"))
        val root: Parent = loader.load()
        mainController = loader.getController()

        val scene = Scene(root, 1200.0, 620.0)

        primaryStage.title = "MobiTool"
        primaryStage.scene = scene

        primaryStage.width = 1400.0
        primaryStage.height = 800.0

        primaryStage.minWidth = 1200.0
        primaryStage.minHeight = 700.0

        primaryStage.maxWidth = 1400.0
        primaryStage.maxHeight = 800.0
    }

    fun start() {
        primaryStage.show()
    }

    fun showDialog(dialog: Stage) {
        Platform.runLater {
            dialog.widthProperty()
                .addListener { _, _, _ -> dialog.x = primaryStage.x + primaryStage.width / 2 - dialog.width / 2 }
            dialog.heightProperty()
                .addListener { _, _, _ -> dialog.y = primaryStage.y + primaryStage.height / 2 - dialog.height / 2 }
            dialog.initModality(Modality.APPLICATION_MODAL)
            dialog.initOwner(primaryStage)
            dialog.showAndWait()
        }
    }

    fun showProgressBottomBar(message: String) = mainController.showProgressBottomBar(message)

    fun hideBottomBar() = mainController.hideBottomBar()
}

val mainScreen = MainGUI.getMainScreen()
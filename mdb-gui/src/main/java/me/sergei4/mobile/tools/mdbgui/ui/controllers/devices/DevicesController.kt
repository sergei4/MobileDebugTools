package me.sergei4.mobile.tools.mdbgui.ui.controllers.devices

import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ListView
import javafx.scene.layout.Pane
import me.sergei4.mobile.tools.mdbgui.app.context
import me.sergei4.mobile.tools.mdbgui.app.model.MobileDevice
import java.net.URL
import java.util.*

class DevicesController : Initializable {

    @FXML
    private lateinit var listDevices: ListView<MobileDevice>

    @FXML
    private lateinit var devicePane: Pane

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        listDevices.setCellFactory { DeviceItemCell() }
        listDevices.items = FXCollections.observableArrayList()
        listDevices.selectionModel.selectedItemProperty()
            .addListener { _, oldValue, newValue ->
                if (oldValue == null) context.model.setCurrentDevice(newValue)
                else if (newValue != null && newValue.id != oldValue.id) context.model.setCurrentDevice(newValue)
            }
        context.model.devices
            .observeOn(JavaFxScheduler.platform())
            .subscribe(
                { devices ->
                    devices.forEach { device ->
                        listDevices.items.find { it.id == device.id }
                            ?.apply { listDevices.items[listDevices.items.indexOf(this)] = device }
                            ?: listDevices.items.add(device)
                    }
                    listDevices.selectionModel.let { model -> model.selectedItem ?: model.selectFirst() }
                },
                {})

        cfgDragAndDropEvent()
    }

    @FXML
    fun handleToggleADBClicked() {

    }

    private fun cfgDragAndDropEvent() {

    }
}
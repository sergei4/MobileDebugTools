package me.sergei4.mobile.tools.mdbgui.ui.controllers.devices

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import me.sergei4.mobile.tools.mdbgui.app.model.MobileDevice

class DeviceItemCell : ListCell<MobileDevice>() {

    private val iphoneCellController: IPhoneItemCellController

    init {
        val iphoneCellLoader = FXMLLoader(javaClass.getResource("/fxml/cell/IPhoneCellLayout.fxml"))
        iphoneCellLoader.load<Pane>()
        iphoneCellController = iphoneCellLoader.getController()
    }

    override fun updateItem(device: MobileDevice?, empty: Boolean) {
        super.updateItem(device, empty)
        if (empty || device == null) {
            text = null
            graphic = null
        } else {
            text = null
            when (device) {
                is MobileDevice.Android -> {
                    iphoneCellController.apply {
                        modelName.text = device.model
                        osVersion.text = "android: ${device.api}"

                        online.fill = if(device.connected)
                            Color.web("0x00995c", 1.0)
                        else
                            Color.web("0xff0000", 1.0)
                        graphic = cellLayout
                    }
                }
                is MobileDevice.IPhone -> {
                    iphoneCellController.apply {
                        modelName.text = device.modelName
                        osVersion.text = device.iOsVersion

                        online.fill = if(device.connected)
                            Color.web("0x00995c", 1.0)
                        else
                            Color.web("0xff0000", 1.0)
                        graphic = cellLayout
                    }
                }
            }
            //text = getDeviceDescription(device)
        }
    }

    private fun getDeviceDescription(device: MobileDevice): String {
        return (if (device.connected) "+" else "-") +
                when (device) {
                    is MobileDevice.Android -> "${device.model}: ${device.api}"
                    is MobileDevice.IPhone -> "${device.modelName}: ${device.iOsVersion}"
                    is MobileDevice.UnknownDevice -> "UnknownDevice"
                }
    }
}

class IPhoneItemCellController {
    @FXML
    lateinit var cellLayout: Pane
    @FXML
    lateinit var modelName: Label
    @FXML
    lateinit var osVersion: Label
    @FXML
    lateinit var online: Circle
}
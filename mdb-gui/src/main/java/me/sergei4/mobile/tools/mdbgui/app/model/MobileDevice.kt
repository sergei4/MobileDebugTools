package me.sergei4.mobile.tools.mdbgui.app.model

import me.sergei4.mobile.tools.mdbgui.connection.AdbHelper
import me.sergei4.mobile.tools.mdbgui.os.AppleDeviceProvider

sealed class MobileDevice(
    open val id: String,
    var connected: Boolean = false
) {
    abstract val name: String

    data class Android(
        override val id: String,
        val model: String,
        val api: String
    ) : MobileDevice(id) {

        override val name: String = "${model}_${api}"

        companion object {
            fun compose(adbHelper: AdbHelper, adbLine: String): Android {
                val properties = adbLine.split(" ")
                val id = properties[0]
                var model = "UNKNOWN"
                properties.forEach { prop ->
                    if (prop.contains("model:")) {
                        model = prop.split("model:")[1]
                    }
                }
                val api = adbHelper.getApi(id)
                return Android(id, model, api)
            }
        }
    }

    data class IPhone(
        override val id: String,
        val model: String,
        val iOsVersion: String
    ) : MobileDevice(id) {
        override val name: String = "${model}_${iOsVersion}"
        val modelName = AppleDeviceProvider.devices[model] ?: model
    }

    data class UnknownDevice(override val name: String = "Unknown device") : MobileDevice("")
}
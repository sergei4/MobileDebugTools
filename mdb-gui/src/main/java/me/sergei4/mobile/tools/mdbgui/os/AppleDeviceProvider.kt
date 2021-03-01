package me.sergei4.mobile.tools.mdbgui.os

object AppleDeviceProvider {
    val devices: Map<String, String>

    init {
        val devicesFromFile = mutableMapOf<String, String>()
        javaClass.getResourceAsStream("/apple_mobile_device_types.txt")
            .bufferedReader()
            .forEachLine { line ->
                line.takeIf { it.isNotEmpty() }?.let { deviceLine ->
                    deviceLine.split(":").takeIf { it.size == 2 }?.let { pair ->
                        devicesFromFile[pair[0].trim()] = pair[1].trim()
                    }
                }
            }
        devices = devicesFromFile
    }
}
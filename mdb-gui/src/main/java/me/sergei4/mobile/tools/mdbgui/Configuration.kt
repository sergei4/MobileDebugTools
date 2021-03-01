package me.sergei4.mobile.tools.mdbgui

import me.sergei4.mobile.tools.mdbgui.connection.AdbHelper
import me.sergei4.mobile.tools.mdbgui.connection.IdeviceHelper
import java.io.File

class Configuration(
    private val rootPath: File,
    adbExecLocation: File,
    iphoneToolsLocation: File
) {
    val adbHelper = AdbHelper(adbExecLocation.absoluteFile)

    val ideviceHelper = IdeviceHelper(iphoneToolsLocation)

    val screenCapturePath: File by lazy {
        val folder = File(rootPath, "screenshots")
        folder.mkdirs()
        return@lazy folder
    }

    val logsPath: File by lazy {
        val folder = File(rootPath, "devicelogs")
        folder.mkdirs()
        return@lazy folder
    }

    val iphonePlatformPath: File by lazy {
        val folder = File(rootPath, "iPhoneOS.platform")
        folder.mkdirs()
        return@lazy folder
    }
}
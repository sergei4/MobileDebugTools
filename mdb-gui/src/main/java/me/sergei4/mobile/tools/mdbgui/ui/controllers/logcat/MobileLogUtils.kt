package me.sergei4.mobile.tools.mdbgui.ui.controllers.logcat

import io.reactivex.Observable
import me.sergei4.mobile.tools.mdbgui.app.Context
import me.sergei4.mobile.tools.mdbgui.app.model.MobileDevice
import me.sergei4.mobile.tools.mdbgui.app.model.MobileDeviceLogLine
import me.sergei4.mobile.tools.mdbgui.extentions.add
import me.sergei4.mobile.tools.mdbgui.extentions.currentTimeStamp
import java.io.File
import java.io.PrintWriter

object MobileLogUtils {
    @Throws(Exception::class)
    fun save(context: Context, device: MobileDevice, lines: Observable<MobileDeviceLogLine>, onFinish: () -> Unit) {
        val logFile: File = context.configuration.logsPath
            .add("${device.name}_${currentTimeStamp()}.log")
        val writer = PrintWriter(logFile, "UTF-8")
        when(device) {
            is MobileDevice.Android -> {
                writer.println("Platform: Android")
                writer.println("Model: " + device.model)
                writer.println("API: " + device.api)
            }
            is MobileDevice.IPhone -> {
                writer.println("Platform: IOS")
                writer.println("Model: " + device.model)
                writer.println("Version OS: " + device.iOsVersion)
            }
            is MobileDevice.UnknownDevice -> {}
        }
        writer.println()
        lines.map { obj: MobileDeviceLogLine -> obj.toString() }
            .subscribe(
                { writer.println(it.toString()) },
                { onFinish.invoke() },
                { onFinish.invoke() }
            )
    }
}
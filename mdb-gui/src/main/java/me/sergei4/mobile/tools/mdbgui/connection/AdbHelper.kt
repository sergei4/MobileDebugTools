package me.sergei4.mobile.tools.mdbgui.connection

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import me.sergei4.mobile.tools.mdbgui.ErrorCodes
import me.sergei4.mobile.tools.mdbgui.app.Result
import me.sergei4.mobile.tools.mdbgui.os.ProcessHelper
import java.io.File

class AdbHelper(private val adbExecPath: File) {

    private fun composeCommand(command: String): String {
        return composeCommand(null, command)
    }

    private fun composeCommand(deviceId: String?, command: String): String {

        val cmd =
            if (command.startsWith("adb"))
                command.replaceFirst("adb", "", ignoreCase = true)
            else command



        return (adbExecPath.absolutePath + File.separator + "adb " +
                if (deviceId == null) cmd else "-s $deviceId $cmd")
        //.apply { println(this)  }
    }

    fun killServer() {
        ProcessHelper.execute(composeCommand("kill-server"))
    }

    fun deviceList(): List<String> {
        return ProcessHelper.execute(composeCommand("devices -l"))
            .split("\n")
            .filter { it.isNotEmpty() }
            .filter { !it.contains("List of devices attached") }
            .filter { !it.startsWith("*") }
    }

    fun getApi(deviceId: String): String {
        return ProcessHelper.execute(composeCommand(deviceId, "shell getprop ro.build.version.release"))
            .split("\n")
            .run { if (isNotEmpty()) this[0] else "" }
    }

    fun observeLogcat(deviceId: String, lines: Int = 0): Observable<String> {
        val command = "logcat" +
                if (lines == 0) "" else " -t $lines"
        return ProcessHelper.observeProcess(composeCommand(deviceId, command))
            .subscribeOn(Schedulers.io())
            .filter { it.isNotEmpty() }
    }

    fun createScreenshot(deviceId: String, tmpFile: String): Result {
        val result = ProcessHelper.execute(composeCommand(deviceId, "adb shell screencap -p $tmpFile"))
        return if (result.isEmpty()) Result.Success() else Result.Failed(ErrorCodes.ANDROID_CANT_CREATE_SCREENSHOT_ON_DEVICE)
    }

    fun pull(deviceId: String, from: String, to: String): Result {
        val result = ProcessHelper.execute(composeCommand(deviceId, "adb pull $from $to")).apply { println(this) }
        return if(result.trim().contains("pulled")) Result.Success() else Result.Failed(ErrorCodes.ANDROID_CANT_PULL_FILE_FROM_DEVICE)
    }

    fun rm(deviceId: String, file: String): Result {
        val result = ProcessHelper.execute(composeCommand(deviceId, "adb shell rm $file"))
        return if (result.isEmpty()) Result.Success() else Result.Failed(ErrorCodes.ANDROID_CANT_REMOVE_FILE_ON_DEVICE)
    }

    fun openLangSettings(deviceId: String) {
        ProcessHelper.execute(composeCommand(deviceId, "shell am start -n com.android.settings/.LanguageSettings"))
    }

    fun getProcessList(deviceId: String): List<String> {
        return ProcessHelper.execute(composeCommand(deviceId, "shell ps"))
            .split("\n")
    }
}
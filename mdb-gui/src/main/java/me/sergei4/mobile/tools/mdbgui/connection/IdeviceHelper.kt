package me.sergei4.mobile.tools.mdbgui.connection

import io.reactivex.Observable
import me.sergei4.mobile.tools.mdbgui.ErrorCodes
import me.sergei4.mobile.tools.mdbgui.app.Result
import me.sergei4.mobile.tools.mdbgui.os.ProcessHelper
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


class IdeviceHelper(private val rootPath: File) {

    private fun composeCommand(command: String): String = composeCommand(command, "")

    private fun composeCommand(command: String, vararg args: String) =
        StringBuilder(File(rootPath, command).absolutePath).apply {
            args.forEach { append(it) }
        }.toString()

    fun deviceList(): String {
        return ProcessHelper.execute(composeCommand(command = "idevice_id", " -l"))
    }

    fun getDeviceProperties(deviceId: String): String {
        return ProcessHelper.execute(composeCommand(command = "ideviceinfo", " -u $deviceId"))
    }

    fun observeDeviceLog(deviceId: String): Observable<String> =
        ProcessHelper.observeProcess(composeCommand(command = "idevicesyslog", " -u $deviceId -d"))
            .filter { it.isNotEmpty() }

    fun createScreenshot(deviceId: String, outFile: String): Result {
        val result = ProcessHelper.execute(composeCommand(command = "idevicescreenshot", " -u $deviceId $outFile"))
        if (result.contains("Could not start")) {
            return Result(false, ErrorCodes.IPHONE_NEED_MOUNT_IMAGE)
        }
        return Result(true)
    }

    fun hasPlatformImage(platformRootPath: File, platformCode: String): Boolean {
        return File(platformRootPath, getMajorPlatformCode(platformCode)).exists()
    }

    fun downloadPlatformImage(platformRootPath: File, platformCode: String) =
        try {
            val platformPath = File(platformRootPath, getMajorPlatformCode(platformCode))
            platformPath.mkdirs()
            downloadFile(
                "http://94.250.253.136:8080/iPhoneOS.platform/${getMajorPlatformCode(platformCode)}/DeveloperDiskImage.dmg",
                File(platformPath, "DeveloperDiskImage.dmg")
            )
            downloadFile(
                "http://94.250.253.136:8080/iPhoneOS.platform/${getMajorPlatformCode(platformCode)}/DeveloperDiskImage.dmg.signature",
                File(platformPath, "DeveloperDiskImage.dmg.signature")
            )
            Result(true)
        } catch (error: IOException) {
            Result(false, ErrorCodes.IPHONE_IMAGE_DOWNLOAD_FAILED)
        }

    private fun downloadFile(url: String, dest: File) {
        val inputStream = URL(url).openStream()
        Files.copy(
            inputStream,
            Paths.get(dest.toURI()),
            StandardCopyOption.REPLACE_EXISTING
        )
        inputStream.close()
    }

    fun mountImage(deviceId: String, platformRootPath: File, platformCode: String): Result {
        val imageFile = File(platformRootPath, "${getMajorPlatformCode(platformCode)}/DeveloperDiskImage.dmg")
        val result = ProcessHelper.execute(composeCommand(command = "ideviceimagemounter", " -u $deviceId ${imageFile.absolutePath}"))
        if (result.contains("Status: Complete")) {
            return Result(true)
        }
        if (result.contains("Device is locked")) {
            return Result(false, ErrorCodes.IPHONE_DEVICE_LOCKED)
        }
        return Result(false, ErrorCodes.IPHONE_MOUNT_IMAGE_FAILED)
    }

    private fun getMajorPlatformCode(platformCode: String): String {
        return platformCode;
    }
}
package me.sergei4.mobile.tools.mdbgui

import me.sergei4.mobile.tools.mdbgui.app.model.MobileDevice
import me.sergei4.mobile.tools.mdbgui.connection.AdbHelper
import me.sergei4.mobile.tools.mdbgui.extentions.add
import org.junit.Before
import org.junit.Test
import java.io.File

class DeviceMonitorTest {

    private lateinit var adbHelper: AdbHelper

    @Before
    fun setUp() {
        val adbExecPath = File(System.getProperty("user.dir"))
            .add("../platform-tools")
            .add("_nix").absoluteFile
        adbHelper = AdbHelper(adbExecPath)
    }

    @Test
    fun observeDevicesTest() {
        val adbHelper = adbHelper
        adbHelper.deviceList().forEach { str ->
            MobileDevice.Android.compose(adbHelper, str).let { println(it) }
        }
    }

    @Test
    fun observeLogcatTest() {
        val adbHelper = adbHelper
        adbHelper.deviceList()
            .map { str -> MobileDevice.Android.compose(adbHelper, str) }
            .firstOrNull()?.run {
                adbHelper.observeLogcat(id, 100)
                    .blockingSubscribe { println(it) }
            }
    }
}
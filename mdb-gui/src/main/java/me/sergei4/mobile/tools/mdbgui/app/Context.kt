package me.sergei4.mobile.tools.mdbgui.app

import me.sergei4.mobile.tools.mdbgui.Configuration
import me.sergei4.mobile.tools.mdbgui.MainGUI

interface Context {
    val configuration: Configuration
    val resources: Resources
    val model: BaseModel
    fun release()
}

class ContextImpl(
    override val configuration: Configuration,
) : Context {

    override val resources: Resources by lazy {
        Resources().apply {
            addFont("courierFont13", "fonts/cour.ttf", 13.0)
        }
    }

    override val model: BaseModel = BaseModelImpl(
        configuration.adbHelper,
        configuration.ideviceHelper,
        configuration.screenCapturePath,
        configuration.iphonePlatformPath
    )

    override fun release() {
        model.close()
        configuration.adbHelper.killServer()
    }
}

val context: Context = MainGUI.getContext()
package me.sergei4.mobile.tools.mdbgui.app.model

sealed class DeviceProcess {

    abstract val simpleName: String

    data class Android(
        val user: String,
        val pid: String,
        val fullName: String
    ): DeviceProcess() {
        override val simpleName: String
            get() = if (fullName.contains(":")) fullName.substring(0, fullName.indexOf(":")) else fullName
    }
}
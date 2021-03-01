package me.sergei4.mobile.tools.mdbgui.app.model

import java.util.regex.Pattern

sealed class MobileDeviceLogLine(
    open val process: DeviceProcess?
)

data class LogcatLine(
    private val logString: String,
    override val process: DeviceProcess.Android?
) : MobileDeviceLogLine(process) {

    val processSubstr = findProcessSubstr(logString)

    override fun toString() =
        if (processSubstr != null && process != null) {
            logString.replace(processSubstr, "$processSubstr/${process.fullName}")
        } else
            logString

    companion object {
        const val PROCESS_ID_UNKNOWN = "?"
        val processRegexp = Pattern.compile("\\s([\\s\\d]+)\\s\\w")

        fun findProcessSubstr(logLine: String): String? {
            val matcher = processRegexp.matcher(logLine)
            return if (matcher.find()) matcher.group(1).trim() else null
        }

        fun getProcessUuid(logLine: String): String {
            return (findProcessSubstr(logLine)
                ?.split(Regex("\\s+"))
                ?.run { if (size > 1) get(0) else PROCESS_ID_UNKNOWN }) ?: PROCESS_ID_UNKNOWN
        }
    }
}

data class IphoneLogLine(
    private val logString: String
) : MobileDeviceLogLine(null) {
    override fun toString() = logString
}

class EmptyLogLine(): MobileDeviceLogLine(null)
package me.sergei4.mobile.tools.mdbgui.extentions

import java.text.SimpleDateFormat
import java.util.*

fun currentTimeStamp() : String =
    SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(Date())
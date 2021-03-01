package me.sergei4.mobile.tools.mdbgui.extentions

import java.io.File

fun File.add(child: String): File {
    return File(this, child)
}
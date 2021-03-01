package me.sergei4.mobile.tools.mdbgui.app

import javafx.scene.text.Font

class Resources {
    val fonts : MutableMap<String, Font> = mutableMapOf()

    fun addFont(name: String, resource: String, size: Double) {
        fonts[name] = Font.loadFont(javaClass.getResource("/$resource").toString(), size)
    }
}


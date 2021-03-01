package me.sergei4.mobile.tools.mdbgui.ui.widgets

import javafx.scene.control.ToggleButton

class RadioToggleButton : ToggleButton() {
    override fun fire() {
        // we don't toggle from selected to not selected if part of a group
        if (toggleGroup == null || !isSelected) {
            super.fire()
        }
    }
}
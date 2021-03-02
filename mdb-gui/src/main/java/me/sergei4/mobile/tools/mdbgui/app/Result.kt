package me.sergei4.mobile.tools.mdbgui.app

sealed class Result (val success: Boolean, val errorCode: String = "") {
    class Success : Result(true)
    class Failed(errorCode: String): Result(false, errorCode)
}
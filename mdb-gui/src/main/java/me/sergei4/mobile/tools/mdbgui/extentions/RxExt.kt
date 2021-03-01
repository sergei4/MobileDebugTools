package me.sergei4.mobile.tools.mdbgui.extentions

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.add(map: MutableMap<String, Disposable>, key: String) {
    map[key] = this
}

fun Disposable.add(disposables: CompositeDisposable) = disposables.add(this)
package me.sergei4.mobile.tools.mdbgui.app.android

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.sergei4.mobile.tools.mdbgui.app.model.DeviceProcess
import me.sergei4.mobile.tools.mdbgui.connection.AdbHelper
import me.sergei4.mobile.tools.mdbgui.extentions.add
import java.util.concurrent.TimeUnit

class ProcessHolder(private val adbHelper: AdbHelper) {

    private val deviceProcessesMap = mutableMapOf<String, MutableList<DeviceProcess.Android>>()
    private val disposableMap = mutableMapOf<String, Disposable>()

    fun getProcesses(deviceId: String): List<DeviceProcess.Android> {
        val processes = deviceProcessesMap.getOrDefault(deviceId, mutableListOf())
        val result = mutableListOf<DeviceProcess.Android>()
        synchronized(processes) {
            result.addAll(processes)
        }
        return result
    }

    fun startCollectProcesses(deviceId: String) {
        val disposable = disposableMap[deviceId]
        if (disposable != null && !disposable.isDisposed) {
            return
        }
        deviceProcessesMap[deviceId] = mutableListOf()
        Observable.interval(0, 4, TimeUnit.SECONDS)
            .flatMapSingle { createProcessObserver(deviceId) }
            .subscribe { newList ->
                val processes = deviceProcessesMap[deviceId]!!
                synchronized(processes) {
                    newList.forEach { if (!processes.contains(it)) processes.add(it) }
                }
            }
            .add(disposableMap, deviceId)
    }

    fun stopCollectProcesses(deviceId: String) {
        deviceProcessesMap[deviceId]?.let { deviceProcessesMap[deviceId] = mutableListOf() }
        disposableMap[deviceId]?.dispose()
    }

    fun destroy() {
        disposableMap.values.forEach { it.dispose() }
    }

    private fun createProcessObserver(deviceId: String) = Single.create<List<DeviceProcess.Android>> { emitter ->
        val processList = adbHelper.getProcessList(deviceId)
        processList
            .filter { it.isNotEmpty() }
            .filter { !it.contains("USER") }
            .map { processLine ->
                processLine.split(Regex("\\s+")).run {
                    val user = this[0]
                    val pid = this[1]
                    val name = this[this.size - 1]
                    DeviceProcess.Android(user, pid, name)
                }
            }.let { emitter.onSuccess(it) }
    }.subscribeOn(Schedulers.io())
}
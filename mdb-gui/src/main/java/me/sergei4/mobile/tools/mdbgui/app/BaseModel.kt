package me.sergei4.mobile.tools.mdbgui.app

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import me.sergei4.mobile.tools.mdbgui.app.android.ProcessHolder
import me.sergei4.mobile.tools.mdbgui.app.model.*
import me.sergei4.mobile.tools.mdbgui.connection.AdbHelper
import me.sergei4.mobile.tools.mdbgui.connection.IdeviceHelper
import java.io.File
import java.io.StringReader
import java.util.*
import java.util.concurrent.TimeUnit

interface BaseModel {
    val devices: Observable<List<MobileDevice>>
    val currentDevice: Observable<MobileDevice>
    fun setCurrentDevice(device: MobileDevice)
    fun createScreenSnapshot(): Single<ScreenShot>
    fun observeLog(): Observable<MobileDeviceLogLine>
    fun observeProcesses(): Observable<List<DeviceProcess>>
    fun mountIOSImage(): Completable
    fun openLangSettings()
    fun close()
}

class BaseModelImpl(
    private val adbHelper: AdbHelper,
    private val ideviceHelper: IdeviceHelper,
    private val screenCapturePath: File,
    private val iphonePlatformPath: File
) : BaseModel {

    private val cachedDevices = mutableListOf<MobileDevice>()
    private val androidProcessHolder = ProcessHolder(adbHelper)

    private val currentDeviceSbj = BehaviorSubject.createDefault<MobileDevice>(MobileDevice.UnknownDevice())

    override val currentDevice: Observable<MobileDevice> = currentDeviceSbj.distinctUntilChanged()

    override fun setCurrentDevice(device: MobileDevice) = currentDeviceSbj.onNext(device)

    override fun close() {
        androidProcessHolder.destroy()
    }

    private val androidDevices: Observable<List<MobileDevice>> =
        Observable.interval(0, 1, TimeUnit.SECONDS)
            .map { adbHelper.deviceList() }
            .switchMapSingle {
                Observable.fromIterable(it)
                    .map { str -> MobileDevice.Android.compose(adbHelper, str) as MobileDevice }
                    .doOnNext { device -> device.connected = true }
                    .toList()
            }

    private val iphoneDevices: Observable<List<MobileDevice>> =
        Observable.interval(0, 1, TimeUnit.SECONDS)
            .map { ideviceHelper.deviceList().split(Regex("\n")).filter { it.isNotEmpty() } }
            .flatMapSingle { uuids ->
                Observable.fromIterable(uuids)
                    .map { Pair(it, ideviceHelper.getDeviceProperties(it)) }
                    .map { pair ->
                        val deviceId = pair.first
                        val props = Properties()
                        var model = ""
                        var iOsVersion = ""
                        try {
                            props.load(StringReader(pair.second))
                            model = props.getProperty("ProductType")
                            iOsVersion = props.getProperty("ProductVersion")
                            return@map MobileDevice.IPhone(deviceId, model, iOsVersion).apply {
                                connected = true
                            }
                        } catch (ex: Exception) {
                            return@map MobileDevice.UnknownDevice()
                        }
                    }.toList()
            }.subscribeOn(Schedulers.io())

    override val devices: Observable<List<MobileDevice>> =
        Observable.combineLatest(androidDevices, iphoneDevices, { androids, iphones ->
            val result = mutableListOf<MobileDevice>()
            result.addAll(androids)
            result.addAll(iphones)
            result
        }).map {
            updateCachedDeviceList(it)
            cachedDevices.forEach { device ->
                when (device) {
                    is MobileDevice.Android -> {
                        if (device.connected)
                            androidProcessHolder.startCollectProcesses(device.id)
                        else
                            androidProcessHolder.stopCollectProcesses(device.id)
                    }
                    is MobileDevice.IPhone -> {
                    }
                    is MobileDevice.UnknownDevice -> {
                    }
                }
            }
            return@map cachedDevices as List<MobileDevice>
        }.share()

    private fun updateCachedDeviceList(devices: List<MobileDevice>) {
        cachedDevices.replaceAll { cachedItem ->
            devices.find { it.id == cachedItem.id } ?: cachedItem.apply { connected = false }
        }
        devices.forEach { device ->
            cachedDevices.find { it.id == device.id } ?: cachedDevices.add(device)
        }
    }

    override fun createScreenSnapshot(): Single<ScreenShot> {
        return when (val currentDevice = currentDeviceSbj.value!!) {
            is MobileDevice.Android -> {
                cachedDevices.find { it.id == currentDevice.id && it.connected } ?: return Single.never()
                Single.create<ScreenShot> {
                    val tempPicture = "/sdcard/temp.png"
                    if (!adbHelper.createScreenshot(currentDevice.id, tempPicture)) {
                        it.onError(Error("Can't create screenshot on device"))
                    }
                    val tmpFile = File(screenCapturePath, "temp_snapshot.png")
                    adbHelper.pull(currentDevice.id, tempPicture, tmpFile.absolutePath)
                    adbHelper.rm(currentDevice.id, tempPicture)
                    it.onSuccess(ScreenShot(currentDevice, tmpFile))
                }.subscribeOn(Schedulers.io())
            }
            is MobileDevice.IPhone -> {
                cachedDevices.find { it.id == currentDevice.id && it.connected } ?: return Single.never()
                Single.create<ScreenShot> {
                    //it.onError(Error(ErrorCodes.IPHONE_NEED_MOUNT_IMAGE))
                    val tmpFile = File(screenCapturePath, "temp_ios_snapshot.png")
                    val result = ideviceHelper.createScreenshot(currentDevice.id, tmpFile.absolutePath)
                    if (result.success) {
                        it.onSuccess(ScreenShot(currentDevice, tmpFile))
                    } else {
                        it.onError(Error(result.errorCode))
                    }
                }.subscribeOn(Schedulers.io())
            }
            is MobileDevice.UnknownDevice -> Single.never()
        }
    }

    override fun observeLog(): Observable<MobileDeviceLogLine> {
        return when (val currentDevice = currentDeviceSbj.value!!) {
            is MobileDevice.Android -> {
                val deviceId = cachedDevices.find { it.id == currentDevice.id && it.connected }?.id
                    ?: return Observable.error(Exception("Device isn't ready yet"))

                adbHelper.observeLogcat(deviceId)
                    .map { logLine ->
                        val process = androidProcessHolder.getProcesses(deviceId)
                            .find { it.pid == LogcatLine.getProcessUuid(logLine) }
                        LogcatLine(logLine, process) as MobileDeviceLogLine
                    }.subscribeOn(Schedulers.io())
            }
            is MobileDevice.IPhone -> {
                val deviceId = cachedDevices.find { it.id == currentDevice.id && it.connected }?.id
                    ?: return Observable.error(Exception("Device isn't ready yet"))

                ideviceHelper.observeDeviceLog(deviceId)
                    .map { IphoneLogLine(it) as MobileDeviceLogLine }
                    .subscribeOn(Schedulers.io())
            }
            is MobileDevice.UnknownDevice -> Observable.empty()
        }
    }

    override fun observeProcesses(): Observable<List<DeviceProcess>> {
        return when (val currentDevice = currentDeviceSbj.value!!) {
            is MobileDevice.Android -> {
                val deviceId = cachedDevices.find { it.id == currentDevice.id && it.connected }?.id
                    ?: return Observable.empty()
                Observable.interval(0, 1, TimeUnit.SECONDS)
                    .map {
                        androidProcessHolder.getProcesses(deviceId)
                            .filter { it.user.startsWith("u") }
                    }
            }
            is MobileDevice.IPhone -> Observable.interval(0, 1, TimeUnit.MINUTES)
                .map { emptyList<DeviceProcess>() }
            is MobileDevice.UnknownDevice -> Observable.empty()
        }
    }

    override fun openLangSettings() {
        when (val currentDevice = currentDeviceSbj.value!!) {
            is MobileDevice.Android -> adbHelper.openLangSettings(currentDevice.id)
        }
    }

    override fun mountIOSImage(): Completable = Completable.fromRunnable {
        val device = currentDeviceSbj.value!! as MobileDevice.IPhone
        if (!ideviceHelper.hasPlatformImage(iphonePlatformPath, device.iOsVersion)) {
            val result = ideviceHelper.downloadPlatformImage(iphonePlatformPath, device.iOsVersion)
            if(!result.success) throw Error(result.errorCode)
        }
        val result = ideviceHelper.mountImage(device.id, iphonePlatformPath, device.iOsVersion)
        if (!result.success) throw Error(result.errorCode)
    }.subscribeOn(Schedulers.io())
}
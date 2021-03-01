package me.sergei4.mobile.tools.mdbgui.ui.controllers.screencapture

import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import me.sergei4.mobile.tools.mdbgui.ErrorCodes
import me.sergei4.mobile.tools.mdbgui.app.BaseModel
import me.sergei4.mobile.tools.mdbgui.app.context
import me.sergei4.mobile.tools.mdbgui.app.model.ScreenShot
import me.sergei4.mobile.tools.mdbgui.extentions.currentTimeStamp
import me.sergei4.mobile.tools.mdbgui.ui.mainScreen
import net.coobird.thumbnailator.Thumbnails
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

class ScreenCaptureController : Initializable {

    private lateinit var model: BaseModel

    @FXML
    private lateinit var paneImageContainer: Pane

    @FXML
    private lateinit var imageViewCapture: ImageView

    @FXML
    private lateinit var btnOpenInEditor: Button

    private var currentScreenShot: ScreenShot? = null

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        model = context.model
    }

    fun init() {

    }

    fun onCreateSnapshotClicked() {
        model.createScreenSnapshot()
            .observeOn(JavaFxScheduler.platform())
            .subscribe(
                {
                    currentScreenShot = it
                    imageViewCapture.image = Image(it.screenShotFile.toURI().toString())
                    imageViewCapture.fitWidthProperty().bind(paneImageContainer.widthProperty())
                    imageViewCapture.fitHeightProperty().bind(paneImageContainer.heightProperty())
                },
                {
                    when (it.message ?: "") {
                        ErrorCodes.IPHONE_NEED_MOUNT_IMAGE -> mountIOSImage()
                        else -> it.printStackTrace()
                    }
                }
            )
    }

    private fun mountIOSImage() {
        Alert(Alert.AlertType.CONFIRMATION).apply {
            title = "Configuration"
            headerText = null
            contentText = "Could not start screenshot service!\n\n" +
                    "Remember that you have to mount the Developer disk image on your device if you want to use the screenshot service\n\n" +
                    "Mount image mow?"
        }.showAndWait().ifPresent {
            if (it == ButtonType.OK) {
                mainScreen.showProgressBottomBar("Mount developer disk image on device")
                model.mountIOSImage()
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe(
                        {
                            showAlertDialog(Alert.AlertType.INFORMATION, "Mount image", "Image has been mounted successful")
                            mainScreen.hideBottomBar()
                        },
                        {
                            showAlertDialog(Alert.AlertType.ERROR, "Mount image", resolveErrorMessage(it.message ?: ""))
                            mainScreen.hideBottomBar()
                        }
                    )
            }
        }
    }

    private fun resolveErrorMessage(errorCode: String) = when (errorCode) {
        ErrorCodes.IPHONE_DEVICE_LOCKED -> "Device is locked, can't mount. Unlock device and try again"
        ErrorCodes.IPHONE_IMAGE_DOWNLOAD_FAILED -> "Couldn't download image from remote server"
        ErrorCodes.IPHONE_MOUNT_IMAGE_FAILED -> "Couldn't mount image on device"
        else -> "Couldn't mount image on device. Reason is unknown"
    }

    private fun showAlertDialog(type: Alert.AlertType, title: String, message: String) {
        Alert(type).apply {
            this.title = title
            headerText = null
            contentText = message
        }.showAndWait()
    }

    fun onSaveClicked() {
        currentScreenShot?.let { screenShot ->
            Thread {
                val snapshotFile = File(context.configuration.screenCapturePath, "${screenShot.device.name}_${currentTimeStamp()}.png")
                val srcImage = SwingFXUtils.fromFXImage(
                    Image(screenShot.screenShotFile.toURI().toString()),
                    null
                )
                val ratio: Float = 400f / srcImage.width
                try {
                    val outImage =
                        Thumbnails.of(srcImage).size((srcImage.width * ratio).toInt(), (srcImage.height * ratio).toInt()).asBufferedImage();
                    ImageIO.write(outImage, "png", snapshotFile);
                } catch (ex: IOException) {
                    ex.printStackTrace();
                }
            }.start()
        }
    }

    @FXML
    fun onOpenInEditorClicked() {

    }

    @FXML
    fun onOpenFolderClicked() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(context.configuration.screenCapturePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
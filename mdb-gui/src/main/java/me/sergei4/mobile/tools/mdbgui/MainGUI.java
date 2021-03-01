package me.sergei4.mobile.tools.mdbgui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import me.sergei4.mobile.tools.mdbgui.app.Context;
import me.sergei4.mobile.tools.mdbgui.app.ContextImpl;
import me.sergei4.mobile.tools.mdbgui.ui.MainScreen;

import java.io.File;

public class MainGUI extends Application {

    private static Context context;
    private static MainScreen mainScreen;

    public static void main(String[] args) {
        launch(args);
    }

    public static Context getContext() {
        return context;
    }

    public static MainScreen getMainScreen() {
        return mainScreen;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Configuration configuration = createConfiguration();

        context = new ContextImpl(configuration);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                context.release();
                Platform.exit();
                System.exit(0);
            }
        });
        mainScreen = new MainScreen(primaryStage);
        mainScreen.start();
    }

    private Configuration createConfiguration() throws Exception {
        File rootPath = new File(System.getProperty("user.dir"));
        File logFolder = new File(rootPath, "logs");

        if (!logFolder.exists()) {
            logFolder.mkdir();
        }

//        System.setErr(new PrintStream(new File(logFolder, "err_" + DateExKt.currentTimeStamp() + ".txt")));
//        System.setOut(new PrintStream(new File(logFolder, "log_" + DateExKt.currentTimeStamp() + ".txt")));

        File adbExecLocation = new File(rootPath, "platform-tools");
        File iphoneToolsLocation = new File(rootPath, "platform-tools");

        return new Configuration(
                rootPath,
                adbExecLocation,
                iphoneToolsLocation
        );
    }
}
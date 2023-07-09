package xxAROX.PresenceMan.Application;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xxAROX.PresenceMan.Application.entity.XboxUserInfo;
import xxAROX.PresenceMan.Application.task.Connection;
import xxAROX.PresenceMan.Application.task.UpdateCheckTask;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.utils.CacheManager;
import xxAROX.PresenceMan.Application.utils.Tray;

import javax.swing.*;

@Getter
@ToString
public final class App {
    private static App instance;
    public static App getInstance() {
        return instance;
    }

    private final Logger logger = LogManager.getLogger(AppInfo.name);
    public static AppUI ui;
    public static Connection connection;

    @Setter private XboxUserInfo xboxUserInfo = null;

    @SneakyThrows
    public App() {
        if (instance != null) {
            System.exit(0);
            return;
        }
        instance = this;
        Thread updateCheckThread = new Thread(new UpdateCheckTask(), "UpdateChecker");
        SwingUtilities.invokeLater(() -> ui = new AppUI());
        updateCheckThread.start();
        xboxUserInfo = CacheManager.loadXboxUserInfo();

        while (ui == null) {
            logger.info("Waiting for UI to be initialized..");
            Thread.sleep(1000);
        }
        ui.setReady();
        new Tray();
    }

    public void shutdown() {
        getLogger().info("Shutting down..");
    }

    public void onLogin() {
        connection = new Connection();
    }

    public void onLogout() {
        connection.close();
    }
}

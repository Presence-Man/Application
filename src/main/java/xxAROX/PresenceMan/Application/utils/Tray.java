package xxAROX.PresenceMan.Application.utils;

import lombok.Getter;
import lombok.ToString;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Getter
 @ToString
public final class Tray {
     private boolean visible = false;
     private final TrayIcon tray_icon;



    public Tray() {
        tray_icon = new TrayIcon(AppInfo.icon.getImage(), AppInfo.name, null);
        tray_icon.setImageAutoSize(true);
        tray_icon.setToolTip(
                (App.getInstance().getXboxUserInfo() != null ? "Logged in as " + App.getInstance().getXboxUserInfo().getGamertag() + "\n" : "")+
                "Double left click to open the application\n"+
                "Tripple right click to exit the application"
        );
        tray_icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == 1) {
                    App.ui.setVisible(true);
                    hideFromTray();
                } else if (e.getClickCount() == 3 && e.getButton() == 3) {
                    App.getInstance().shutdown();
                    System.exit(0);
                }
            }
        });
    }

    public void showInTray() {
        if (visible) return;
        if (!SystemTray.isSupported()) return;
        tray_icon.setToolTip(
                (App.getInstance().getXboxUserInfo() != null ? "Logged in as " + App.getInstance().getXboxUserInfo().getGamertag() + "\n\n" : "")+
                        "Double left click to open the application\n"+
                        "Tripple right click to exit the application"
        );
        try {SystemTray.getSystemTray().add(tray_icon);visible = true;}
        catch (AWTException e) {App.getInstance().getLogger().error(e);}
    }

    public void hideFromTray() {
        if (!visible) return;
        if (!SystemTray.isSupported()) return;
        SystemTray.getSystemTray().remove(tray_icon);
        visible = false;
    }
}

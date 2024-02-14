/*
 * Copyright (c) 2024. By Jan-Michael Sohn also known as @xxAROX.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xxAROX.PresenceMan.Application.utils;

import lombok.Getter;
import lombok.ToString;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;
import xxAROX.PresenceMan.Application.Bootstrap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

@Getter
 @ToString
public final class Tray {
     private boolean visible = false;
     private final TrayIcon tray_icon;



    public Tray() {
        tray_icon = new TrayIcon(new ImageIcon(Objects.requireNonNull(Bootstrap.class.getClassLoader().getResource(AppInfo.icon))).getImage(), AppInfo.name, null);
        tray_icon.setImageAutoSize(true);
        tray_icon.setToolTip(
                "Double left click to open the application\n"+
                "Tripple right click to exit the application"
        );
        tray_icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == 1) {
                    App.ui.setVisible(true);
                    App.ui.setExtendedState(JFrame.NORMAL);
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
                "Double left click to open the application\n"+
                "Tripple right click to exit the application"
        );
        try {SystemTray.getSystemTray().add(tray_icon);visible = true;}
        catch (AWTException e) {App.getLogger().error(e);}
    }

    public void hideFromTray() {
        if (!visible) return;
        if (!SystemTray.isSupported()) return;
        SystemTray.getSystemTray().remove(tray_icon);
        visible = false;
    }
}

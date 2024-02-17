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
import xxAROX.PresenceMan.Application.task.UpdateCheckTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
 @ToString
public final class Tray {
    private static TrayIcon tray = null;
    private static boolean visible = false;



    public static TrayIcon build() {
        return build(default_items());
    }
    public static TrayIcon build(List<JMenuItem> menuItems) {
        if (tray != null) return tray;
        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        JPopupMenu trayMenu = new JPopupMenu() {@Override protected void firePopupMenuWillBecomeInvisible() {dialog.setVisible(false);super.firePopupMenuWillBecomeInvisible();}};
        for (JMenuItem menuItem : menuItems) {
            if (menuItem == null) trayMenu.addSeparator();
            else trayMenu.add(menuItem);
        }

        var tray = new TrayIcon(new ImageIcon(Objects.requireNonNull(Bootstrap.class.getClassLoader().getResource(AppInfo.icon))).getImage(), AppInfo.name, null);
        tray.setImageAutoSize(true);
        tray.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    App.ui.setVisible(true);
                    Tray.hideFromTray();
                } else if (e.getButton() == MouseEvent.BUTTON3 && e.isPopupTrigger()) {
                    dialog.setSize(trayMenu.getPreferredSize());
                    dialog.setLocation(e.getX(), e.getY() - trayMenu.getHeight());
                    dialog.setVisible(true);
                    trayMenu.show(dialog, 0, 0);
                }
            }
        });
        return tray;
    }

    private static List<JMenuItem> default_items() {
        List<JMenuItem> arr = new ArrayList<>();

        {
            var title = new JMenuItem(title());
            title.setEnabled(false);

            arr.add(title);
        }

        {
            var exit = new JMenuItem("Check for updates");
            exit.addActionListener(e -> App.getInstance().getScheduler().scheduleAsync(new UpdateCheckTask(true)));
            arr.add(exit);
        }

        {
            arr.add(null);
        }

        {
            var exit = new JMenuItem("Exit");
            exit.addActionListener(e -> {
                if (!App.getInstance().isShutdown()) App.getInstance().shutdown();
                System.exit(0);
            });
            arr.add(exit);
        }
        return arr;
    }

    private static String title() {
        return AppInfo.name + " - v" + AppInfo.getVersion();
    }

    public static void showInTray() {
        if (visible) return;
        if (!SystemTray.isSupported()) return;
        tray = build();
        tray.setToolTip(title());
        try {SystemTray.getSystemTray().add(tray);visible = true;}
        catch (AWTException e) {App.getLogger().error(e);}
    }

    public static void hideFromTray() {
        if (!visible) return;
        if (!SystemTray.isSupported()) return;
        SystemTray.getSystemTray().remove(tray);
        visible = false;
    }
}

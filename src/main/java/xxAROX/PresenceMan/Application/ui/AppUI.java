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

package xxAROX.PresenceMan.Application.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightIJTheme;
import com.jthemedetecor.OsThemeDetector;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;
import xxAROX.PresenceMan.Application.Bootstrap;
import xxAROX.PresenceMan.Application.ui.tabs.FeaturedServersTab;
import xxAROX.PresenceMan.Application.ui.tabs.GeneralTab;
import xxAROX.PresenceMan.Application.ui.tabs.LoginTab;
import xxAROX.PresenceMan.Application.ui.tabs.PrivacyPolicyTab;
import xxAROX.PresenceMan.Application.utils.Tray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AppUI extends JFrame {
    public final JTabbedPane contentPane = new JTabbedPane();
    private final List<AUITab> tabs = new ArrayList<>();
    private final Tray tray = new Tray();

    public final GeneralTab general_tab = new GeneralTab(this);
    public final LoginTab login_tab = new LoginTab(this);
    public final PrivacyPolicyTab privacy_policy_tab = new PrivacyPolicyTab(this);
    public final FeaturedServersTab featured_servers_tab = new FeaturedServersTab(this);

    public AppUI() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> showException(e));

        this.setLookAndFeel();
        this.initWindow();
        {
            contentPane.setLayout(new GridBagLayout());
            AUITab last = null;
            for (Field field : Arrays.stream(this.getClass().getFields()).filter(field -> AUITab.class.isAssignableFrom(field.getType())).toList()) {
                try {
                    var tab = (AUITab) field.get(this);
                    tabs.add(tab);
                    tab.add(contentPane);
                    last = tab;
                } catch (IllegalAccessException e) {
                    App.getLogger().error(e);
                }
            }
            assert last != null;
            contentPane.setEnabledAt(contentPane.indexOfTab(last.getName()), false);
        }
        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(10_000);
        SwingUtilities.updateComponentTreeUI(this);
        setVisible(false);
        setExtendedState(JFrame.ICONIFIED);
        tray.showInTray();
    }

    private void setLookAndFeel() {
        try {
            final OsThemeDetector detector = OsThemeDetector.getDetector();
            final boolean isDarkThemeUsed = detector.isDark();
            detector.registerListener(isDark -> SwingUtilities.invokeLater(() -> {
                if (isDark) {
                    FlatAtomOneDarkIJTheme.setup();
                    FlatAtomOneDarkIJTheme.setPreferredFontFamily(FlatLaf.getPreferredMonospacedFontFamily());
                } else {
                    FlatAtomOneLightIJTheme.setup();
                    FlatAtomOneLightIJTheme.setPreferredFontFamily(FlatLaf.getPreferredMonospacedFontFamily());
                }
                FlatLaf.updateUI();
            }));
            if (isDarkThemeUsed) {
                FlatAtomOneDarkIJTheme.setup();
                FlatAtomOneDarkIJTheme.setPreferredFontFamily(FlatLaf.getPreferredMonospacedFontFamily());
            } else {
                FlatAtomOneLightIJTheme.setup();
                FlatAtomOneLightIJTheme.setPreferredFontFamily(FlatLaf.getPreferredMonospacedFontFamily());
            }
            FlatLaf.updateUI();
        } catch (Throwable t) {
            App.getLogger().error(t);
        }
    }

    private void initWindow() {
        setTitle(AppInfo.name + " v" + AppInfo.getVersion());
        setIconImage(new ImageIcon(Objects.requireNonNull(Bootstrap.class.getClassLoader().getResource(AppInfo.icon))).getImage());
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (AUITab tab : tabs) tab.onClose();
                tray.showInTray();
                setVisible(false);
            }
        });
        setSize(600, 360);
        setResizable(false);
        setLocationRelativeTo(null);
        setContentPane(contentPane);
    }

    public void setReady() {
        for (AUITab tab : tabs) tab.setReady();
        for (int i=0; i<contentPane.getTabCount(); i++) contentPane.setEnabledAt(i, true);
    }

    public void openURL(final String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Throwable t) {
            this.showInfo("Couldn't open the link :(\nHere it is for you: " + url);
        }
    }

    public void showException(Throwable t) {
        t.printStackTrace();
        /*
         * StringBuilder builder = new StringBuilder("An error occurred:\n");
         * builder.append("[").append(t.getClass().getSimpleName()).append("] ").append(t.getMessage()).append("\n");
         * for (StackTraceElement element : t.getStackTrace()) builder.append(element.toString()).append("\n");
         * showError(builder.toString());
         */
    }

    public void showInfo(String message) {
        this.showNotification(message, JOptionPane.INFORMATION_MESSAGE);
    }

    public void showWarning(String message) {
        this.showNotification(message, JOptionPane.WARNING_MESSAGE);
    }

    public void showError(String message) {
        this.showNotification(message, JOptionPane.ERROR_MESSAGE);
    }

    public void showNotification(String message, int type) {
        JOptionPane.showMessageDialog(this, message, AppInfo.name, type);
    }
}

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

package xxAROX.PresenceMan.Application.task;

import com.vdurmont.semver4j.Semver;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;
import xxAROX.PresenceMan.Application.ui.popup.DownloadPopup;
import xxAROX.PresenceMan.Application.utils.Lang;
import xxAROX.PresenceMan.Application.utils.Utils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@AllArgsConstructor
@NoArgsConstructor
public class UpdateCheckTask implements Runnable {
    private boolean show_up_to_date_dialog = false;
    @Override
    public void run() {
        try {
            String latestVersion = Utils.WebUtils.get("https://raw.githubusercontent.com/Presence-Man/Application/main/latest_version" + (AppInfo.development ? "-dev" : "") + ".txt").getBody().trim();

            boolean updateAvailable;
            try {
                Semver versionSemver = new Semver(AppInfo.getVersion());
                Semver latestVersionSemver = new Semver(latestVersion);
                updateAvailable = latestVersionSemver.isGreaterThan(versionSemver);
                if (AppInfo.development && versionSemver.isGreaterThan(latestVersionSemver)) App.getLogger().warn("You are running a dev version of Presence-Man");
            } catch (Throwable t) {
                updateAvailable = !AppInfo.getVersion().equals(latestVersion);
            }
            if (updateAvailable) {
                App.getLogger().warn("You are running an outdated version of " + AppInfo.name + "! Latest version: " + latestVersion);
                String latest_url = "https://github.com/Presence-Man/Application/releases/download/" + (AppInfo.development ? "dev" : ("v"+latestVersion)) + "/Presence-Man-App.jar";
                SwingUtilities.invokeLater(() -> this.showUpdateQuestion(latest_url, latestVersion));
            } else {
                if (show_up_to_date_dialog) SwingUtilities.invokeLater(() -> App.ui.showInfo("Presence-Man is up to date!"));
            }
        } catch (Throwable ignored) {
        }
    }

    private void showUpdateQuestion(final String downloadUrl, final String latestVersion) {
        int chosen = JOptionPane.showConfirmDialog(
                App.ui,
                Lang.get("ui.popup.updater.message", new HashMap<>(){{put("{latestVersion}", latestVersion);}}),
                Lang.get("ui.popup.updater.title", new HashMap<>(){{put("{latestVersion}", latestVersion);}}),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (chosen == JOptionPane.YES_OPTION) {
            File f = new File(AppInfo.name + "-App" + (AppInfo.development ? "-dev" : "") + " - v" + latestVersion + ".jar");
            new DownloadPopup(
                App.ui,
                downloadUrl,
                f,
                () -> SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(App.ui, Lang.get("ui.popup.updater.done"), AppInfo.name, JOptionPane.INFORMATION_MESSAGE);
                    try {
                        System.out.println(f.getAbsolutePath());
                        Runtime.getRuntime().exec(new String[]{System.getProperty("java.home") + "/bin/java", "-jar", f.getAbsolutePath()});
                        System.exit(0);
                    } catch (IOException e) {
                        App.getLogger().error("Could not start the new jar file", e);
                        App.ui.showException(e);
                    }
                }),
                t -> {
                    if (t != null) App.getLogger().error("Could not download the latest version of " + AppInfo.name, t);
                }
            );
        }
    }
}

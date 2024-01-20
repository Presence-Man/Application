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
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;
import xxAROX.PresenceMan.Application.ui.popup.DownloadPopup;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateCheckTask implements Runnable {
    @Override
    public void run() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/Presence-Man/releases/main/" + (AppInfo.development ? "dev-" : "") + "version-app.txt");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", AppInfo.name + "/" + AppInfo.getVersion());
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            InputStream in = con.getInputStream();
            byte[] bytes = new byte[1024];
            int read;
            StringBuilder builder = new StringBuilder();
            while ((read = in.read(bytes)) != -1) builder.append(new String(bytes, 0, read));
            con.disconnect();
            String latestVersion = builder.toString().trim();

            boolean updateAvailable;
            try {
                Semver versionSemver = new Semver(AppInfo.getVersion());
                Semver latestVersionSemver = new Semver(latestVersion);
                updateAvailable = latestVersionSemver.isGreaterThan(versionSemver);
                if (AppInfo.development || versionSemver.isGreaterThan(latestVersionSemver)) App.getInstance().getLogger().warn("You are running a dev version of PresenceMan");
            } catch (Throwable t) {
                updateAvailable = !AppInfo.getVersion().equals(latestVersion);
            }
            if (updateAvailable) {
                App.getInstance().getLogger().warn("You are running an outdated version of " + AppInfo.name + "! Latest version: " + latestVersion);
                String latest_url = "https://github.com/Presence-Man/releases/releases/download/latest/Presence-Man-App" + (AppInfo.development ? "-dev" : "") + ".jar";
                SwingUtilities.invokeLater(() -> this.showUpdateQuestion(latest_url, latestVersion));
            }
        } catch (Throwable ignored) {
        }
    }

    private void showUpdateQuestion(final String downloadUrl, final String latestVersion) {
        int chosen = JOptionPane.showConfirmDialog(
                App.ui,
                "You are running an outdated version of PresenceMan!\nCurrent version: " + AppInfo.getVersion() + "\nLatest version: " + latestVersion + "\n\nDo you want to update?",
                AppInfo.name + " update",
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
                    JOptionPane.showMessageDialog(App.ui, "Downloaded the latest version of " + AppInfo.name + "!\nPress OK to restart.", AppInfo.name, JOptionPane.INFORMATION_MESSAGE);
                    try {
                        Runtime.getRuntime().exec(new String[]{System.getProperty("java.home") + "/bin/java", "-jar", f.getAbsolutePath()});
                        System.exit(0);
                    } catch (IOException e) {
                        App.getInstance().getLogger().error("Could not start the new jar file", e);
                        App.ui.showException(e);
                    }
                }),
                t -> {
                    if (t != null) {
                        App.getInstance().getLogger().error("Could not download the latest version of " + AppInfo.name, t);
                        App.ui.showException(t);
                    }
                }
            );
        }
    }
}

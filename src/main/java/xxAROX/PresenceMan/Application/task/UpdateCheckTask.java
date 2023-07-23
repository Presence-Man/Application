package xxAROX.PresenceMan.Application.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
            URL url = new URL("https://api.github.com/repos/Presence-Man/Application/releases/latest");
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

            JsonObject object = JsonParser.parseString(builder.toString()).getAsJsonObject();
            String latestVersion = object.get("tag_name").getAsString().substring(1);
            boolean updateAvailable;
            try {
                Semver versionSemver = new Semver(AppInfo.getVersion());
                Semver latestVersionSemver = new Semver(latestVersion);
                updateAvailable = latestVersionSemver.isGreaterThan(versionSemver);
                if (versionSemver.isGreaterThan(latestVersionSemver)) App.getInstance().getLogger().warn("You are running a dev version of PresenceMan");
            } catch (Throwable t) {
                updateAvailable = !AppInfo.getVersion().equals(latestVersion);
            }
            if (updateAvailable) {
                App.getInstance().getLogger().warn("You are running an outdated version of " + AppInfo.name + "! Latest version: " + latestVersion);
                JsonArray assets = object.getAsJsonArray("assets");
                boolean found = false;
                for (JsonElement asset : assets) {
                    JsonObject assetObject = asset.getAsJsonObject();
                    if (isPresenceManJar(object, assetObject)) {
                        found = true;
                        SwingUtilities.invokeLater(() -> this.showUpdateQuestion(assetObject.get("name").getAsString(), assetObject.get("browser_download_url").getAsString(), latestVersion));
                        break;
                    }
                }
                if (!found) SwingUtilities.invokeLater(() -> this.showUpdateWarning(latestVersion));
            }
        } catch (Throwable ignored) {
        }
    }

    private void showUpdateWarning(final String latestVersion) {
        JOptionPane.showMessageDialog(
                App.ui,
                "You are running an outdated version of " + AppInfo.name + "!\nCurrent version: " + AppInfo.getVersion() + "\nLatest version: " + latestVersion, AppInfo.name,
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showUpdateQuestion(final String name, final String downloadUrl, final String latestVersion) {
        int chosen = JOptionPane.showConfirmDialog(
                App.ui,
                "You are running an outdated version of PresenceMan!\nCurrent version: " + AppInfo.getVersion() + "\nLatest version: " + latestVersion + "\n\nDo you want to update?",
                AppInfo.name,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (chosen == JOptionPane.YES_OPTION) {
            File f = new File(name);
            new DownloadPopup(App.ui, downloadUrl, f, () -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(App.ui, "Downloaded the latest version of PresenceMan!\nPress OK to restart.", "PresenceMan", JOptionPane.INFORMATION_MESSAGE);
                    try {
                        Runtime.getRuntime().exec(new String[]{System.getProperty("java.home") + "/bin/java", "-jar", f.getAbsolutePath()});
                        System.exit(0);
                    } catch (IOException e) {
                        App.getInstance().getLogger().error("Could not start the new PresenceMan jar", e);
                        App.ui.showException(e);
                    }
                });
            }, t -> {
                if (t != null) {
                    App.getInstance().getLogger().error("Could not download the latest version of PresenceMan", t);
                    App.ui.showException(t);
                }
            });
        }
    }

    private boolean isPresenceManJar(final JsonObject root, final JsonObject assetObject) {
        return assetObject.get("name").getAsString().equals(root.get("name").getAsString() + ".jar");
    }

}

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

package xxAROX.PresenceMan.Application.ui.popup;

import xxAROX.PresenceMan.Application.AppInfo;
import xxAROX.PresenceMan.Application.ui.AppUI;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public final class DownloadPopup extends JDialog {
    private final AppUI parent;
    private final String url;
    private final File file;
    private final Runnable finishListener;
    private final Consumer<Throwable> stopConsumer;

    private JProgressBar progressBar;
    private Thread downloadThread;

    public DownloadPopup(AppUI parent, String url, File file, Runnable finishListener, Consumer<Throwable> stopConsumer) {
        super(parent, true);
        this.parent = parent;
        this.url = url;
        this.file = file;
        this.finishListener = finishListener;
        this.stopConsumer = stopConsumer;

        this.initWindow();
        this.initComponents();
        this.setVisible(true);
    }

    private void initWindow() {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DownloadPopup.this.close(false);
            }
        });
        this.setTitle("Downloading...");
        this.setSize(400, 110);
        this.setResizable(false);
        this.setLocationRelativeTo(this.parent);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel();
        contentPane.setLayout(null);
        this.setContentPane(contentPane);
        {
            this.progressBar = new JProgressBar();
            this.progressBar.setBounds(10, 10, 365, 20);
            this.progressBar.setStringPainted(true);
            contentPane.add(this.progressBar);
        }
        {
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setFocusPainted(false);
            cancelButton.setBounds(10, 40, 365, 20);
            cancelButton.addActionListener(event -> this.close(false));
            contentPane.add(cancelButton);
        }
        this.start();
    }

    private void start() {
        this.downloadThread = new Thread(() -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(this.url).openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", AppInfo.name + "/" + AppInfo.getVersion());
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                int contentLength = con.getContentLength();
                int current = 0;
                File tempFile = File.createTempFile("Presence-Man-download", "");
                InputStream is = con.getInputStream();
                FileOutputStream fos = new FileOutputStream(tempFile);
                byte[] buffer = new byte[1024 * 1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    if (this.downloadThread.isInterrupted()) throw new InterruptedException();
                    fos.write(buffer, 0, len);

                    if (contentLength != -1) {
                        current += len;
                        this.progressBar.setValue((int) (100F / contentLength * current));
                    }
                }
                fos.close();
                is.close();
                con.disconnect();
                Files.move(tempFile.toPath(), this.file.toPath(), StandardCopyOption.REPLACE_EXISTING);

                this.close(true);
                this.finishListener.run();
            } catch (InterruptedException ignored) {
            } catch (Throwable t) {
                this.close(false);
                this.stopConsumer.accept(t);
            }
        }, "Download Popup Thread");
        this.downloadThread.setDaemon(true);
        this.downloadThread.start();
    }

    private void close(final boolean success) {
        if (!success && this.downloadThread != null && this.downloadThread.isAlive()) {
            this.downloadThread.interrupt();
            this.stopConsumer.accept(null);
        }
        this.setVisible(false);
        this.dispose();
    }
}

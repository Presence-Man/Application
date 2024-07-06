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

import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Utils {
    public static final Gson GSON = new Gson();

    private static Map<String, String> getDefaultParams() {
        return new HashMap<>(){{
            put("{App.name}", AppInfo.name);
            put("{App.version}", AppInfo.getVersion());

            if (App.getInstance().xboxUserInfo != null) {
                put("{xuid}", App.getInstance().xboxUserInfo.getXuid());
                put("{gamertag}", App.getInstance().xboxUserInfo.getGamertag());
            }
            put("{network}", App.getInstance().network != null ? App.getInstance().network : "null");
            put("{server}", App.getInstance().server != null ? App.getInstance().server : "null");

        }};
    }

    public static String replaceParams(String base){
        return replaceParams(base, getDefaultParams());
    }
    public static String replaceParams(String base, Map<String, String> params){
        if (!params.containsKey("{App.name}")) params.putAll(getDefaultParams());
        for (Map.Entry<String, String> keyValueEntry : params.entrySet()) base = base.replace(keyValueEntry.getKey(), keyValueEntry.getValue());
        return base;
    }

    public static class SingleInstanceUtils {
        private static final String LOCK_FILE = System.getProperty("user.home") + "/.presence-man.lock";

        public static boolean lockInstance(Logger logger) {
            try {
                File file = new File(LOCK_FILE);
                RandomAccessFile ac_file = new RandomAccessFile(file, "rw");
                FileLock file_lock = ac_file.getChannel().tryLock();
                if (file_lock != null) {
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            file_lock.release();
                            ac_file.close();
                            file.delete();
                        } catch (IOException e) {
                            logger.error("Unable to remove lock file: " + LOCK_FILE, e);
                        }
                    }));
                    return true;
                }
            } catch (Exception e) {
                logger.error("Unable to create and/or lock file: " + LOCK_FILE, e);
            }
            return false;
        }
    }

    public static class UIUtils {
        public static JButton addButton(JPanel panel, GridBagConstraints constraints, int gridy, String text, Consumer<JButton> handler) {
            JButton button = new JButton(text);
            button.addActionListener(event -> handler.accept(button));
            constraints.gridx = 0;
            constraints.gridy = gridy;
            constraints.gridwidth = 2;
            panel.add(button, constraints);
            return button;
        }

        public static JCheckBox addCheckbox(JPanel panel, GridBagConstraints constraints, int gridy, String text, boolean default_value, Consumer<Boolean> handler) {
            JCheckBox checkBox = new JCheckBox(text, default_value);
            checkBox.addItemListener(event -> {
                boolean value = event.getStateChange() == ItemEvent.SELECTED;
                handler.accept(value);
            });
            constraints.gridx = 0;
            constraints.gridy = gridy;
            constraints.gridwidth = 2;
            panel.add(checkBox, constraints);
            return checkBox;
        }

        public static JSlider addSlider(JPanel panel, GridBagConstraints constraints, int gridy, String label, int min, int max, int initial, Consumer<Integer> handler) {
            JLabel sliderLabel = new JLabel(label + ":");
            constraints.gridx = 0;
            constraints.gridy = gridy;
            constraints.gridwidth = 1;
            panel.add(sliderLabel, constraints);

            JSlider slider = new JSlider(min, max, initial);
            slider.addChangeListener((ChangeEvent e) -> handler.accept(((JSlider) e.getSource()).getValue()));
            constraints.gridx = 1;
            panel.add(slider, constraints);
            return slider;
        }
    }
}

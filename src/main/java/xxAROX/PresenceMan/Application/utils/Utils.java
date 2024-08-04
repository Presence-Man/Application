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
import xxAROX.WebRequester.WebRequester;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            put("{network}", App.getInstance().network_info.network != null ? App.getInstance().network_info.network : "null");
            put("{server}", App.getInstance().network_info.server != null ? App.getInstance().network_info.server : "null");

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




    public static Optional<File> getJarFile() {
        try {return Optional.of(new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI()));} catch (Throwable ignored) {return Optional.empty();}
    }
    public static void launch(File jarFile) throws IOException {
        new ProcessBuilder(System.getProperty("java.home") + "/bin/java", "-jar", jarFile.getAbsolutePath() + (AppInfo.development ? " development" : "")).start();
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
        public static ImageIcon createImageIcon(String path) {
            return createImageIcon(path, null);
        }
        public static ImageIcon createImageIcon(String path, String description) {
            URL imgURL = null;
            try {
                imgURL = path.startsWith("https://") ? new URL(path) : Utils.class.getClassLoader().getResource(path);
            } catch (MalformedURLException ignore) {
            }
            if (imgURL != null) return new ImageIcon(imgURL, description == null ? "" : description);
            App.getLogger().error("Couldn't find image: " + path);
            return null;
        }

        public static JButton addButton(JPanel panel, GridBagConstraints constraints, int gridy, String text, Consumer<JButton> handler) {
            JButton button = new JButton(text);
            button.addActionListener(event -> handler.accept(button));
            constraints.gridx = 0;
            constraints.gridy = gridy;
            constraints.gridwidth = 2;
            panel.add(button, constraints);
            return button;
        }

        public static JButton addButton(JPanel panel, GridBagConstraints constraints, int gridy, String text, Consumer<JButton> handler, Icon i) {
            JButton button = new JButton(text, i);
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
            constraints.gridwidth = 1;
            panel.add(checkBox, constraints);
            return checkBox;
        }

        public static <SameObj> JComboBox<String> addDropdownMenu(JPanel panel, GridBagConstraints constraints, int gridy, String title, Map<String, SameObj> options, String default_value, BiConsumer<String, SameObj> handler) {
            JLabel titleLabel = new JLabel(title);
            constraints.gridx = 0;
            constraints.gridy = gridy;
            constraints.gridwidth = 1;
            panel.add(titleLabel, constraints);

            JComboBox<String> comboBox = new JComboBox<>(options.keySet().toArray(new String[0]));
            comboBox.setSelectedItem(default_value);
            comboBox.addActionListener(event -> handler.accept((String) comboBox.getSelectedItem(), options.get((String) comboBox.getSelectedItem())));
            constraints.gridx = 0;
            constraints.gridy = gridy +1;
            constraints.gridwidth = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 0.5;
            panel.add(comboBox, constraints);
            return comboBox;
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

    public static class ImageUtils {
        public static ImageIcon recolorImage(ImageIcon image, Color newColor) {
            return new ImageIcon(recolorBufferedImage(toBufferedImage(image.getImage(), image.getIconHeight(), image.getIconWidth()), newColor));
        }

        public static BufferedImage toBufferedImage(Image img, int w, int h) {
            if (img instanceof BufferedImage) return (BufferedImage) img;
            BufferedImage bimage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            Graphics2D bGr = bimage.createGraphics();
            bGr.drawImage(img, 0, 0, null);
            bGr.dispose();

            return bimage;
        }

        private static BufferedImage recolorBufferedImage(BufferedImage image, Color newColor) {
            int width = image.getWidth();
            int height = image.getHeight();

            BufferedImage recoloredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgba = image.getRGB(x, y);
                    Color col = new Color(rgba, true);
                    if (col.getAlpha() != 0) recoloredImage.setRGB(x, y, newColor.getRGB());
                    else recoloredImage.setRGB(x, y, rgba);
                }
            }
            return recoloredImage;
        }
    }

    public static class WebUtils {
        public static WebRequester.Result get(String url) {return get(url, new HashMap<>());}
        public static WebRequester.Result get(String url, Map<String, String> headers) {
            WebRequester.init(GSON, App.getInstance().getTickExecutor());
            try {
                var result = WebRequester.get(url, headers).get();
                return result;
            } catch (ExecutionException | InterruptedException e) {
                App.getLogger().error("Error while GET: " + e.getMessage());
                return null;
            }
        }
        public static WebRequester.Result post(String url) {return post(url, new HashMap<>(), new HashMap<>());}
        public static WebRequester.Result post(String url, Map<String, String> headers) {return post(url, headers, new HashMap<>());}
        public static WebRequester.Result post(String url, Map<String, String> headers, Map<String, String> body) {
            WebRequester.init(GSON, App.getInstance().getTickExecutor());
            try {
                var result = WebRequester.post(url, headers, body).get();
                return result;
            } catch (ExecutionException | InterruptedException e) {
                App.getLogger().error("Error while POST: " + e.getMessage());
                return null;
            }
        }
    }

    public static class FileUtils {
        public static Map<Path, byte[]> getFilesInDirectory(final String assetPath) throws IOException, URISyntaxException {
            final Path path = getPath(Utils.class.getClassLoader().getResource(assetPath).toURI());
            return getFilesInPath(path);
        }

        private static Path getPath(final URI uri) throws IOException {
            try {
                return Paths.get(uri);
            } catch (FileSystemNotFoundException e) {
                FileSystems.newFileSystem(uri, Collections.emptyMap());
                return Paths.get(uri);
            }
        }

        private static Map<Path, byte[]> getFilesInPath(final Path path) throws IOException {
            try (Stream<Path> stream = Files.list(path)) {
                return stream
                        .filter(Files::isRegularFile)
                        .sorted(Comparator.comparing(Path::toString))
                        .collect(Collectors.toMap(
                                f -> f,
                                f -> {try {return Files.readAllBytes(f);} catch (IOException e) {throw new UncheckedIOException(e);}},
                                (u, v) -> {throw new IllegalStateException("Duplicate key");},
                                LinkedHashMap::new
                        ))
                ;
            }
        }
    }
}

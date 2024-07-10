/*
 * Copyright (c) 2024. By Jan-Michael Sohn also known as @xxAROX.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xxAROX.PresenceMan.Application.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Lang {
    public static final String DEFAULT_LOCALE = "en_US";
    public static Map<String, Properties> LOCALES = new LinkedHashMap<>();

    static {
        try {
            for (Map.Entry<Path, byte[]> entry : Utils.FileUtils.getFilesInDirectory("languages").entrySet()) {
                final Properties properties = new Properties();
                properties.load(new InputStreamReader(new ByteArrayInputStream(entry.getValue()), StandardCharsets.UTF_8));
                LOCALES.put(entry.getKey().getFileName().toString().replace(".properties", ""), properties);
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load translations", e);
        }
        LOCALES = LOCALES.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().getProperty("language.name")))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new));

        if (CacheManager.Settings.LOCALE == null || !LOCALES.containsKey(CacheManager.Settings.LOCALE)) {
            final String systemLocale = Locale.getDefault().getLanguage() + '_' + Locale.getDefault().getCountry();
            if (LOCALES.containsKey(systemLocale)) CacheManager.Settings.LOCALE = systemLocale;
            else {
                for (Map.Entry<String, Properties> entry : LOCALES.entrySet()) {
                    if (entry.getKey().startsWith(Locale.getDefault().getLanguage() + '_')) {
                        CacheManager.Settings.LOCALE = entry.getKey();
                        break;
                    }
                }
            }
        }
        final int totalTranslation = LOCALES.get(DEFAULT_LOCALE).size();
        for (Properties properties : LOCALES.values()) {
            final int translated = properties.size();
            final float percentage = (float) translated / totalTranslation * 100;
            properties.put("language.completion", (int) Math.floor(percentage) + "%");
        }
    }

    public static String get(final String key) {
        return getSpecific(CacheManager.Settings.LOCALE, key);
    }

    public static String get(final String key, Map<String, String> args) {
        return getSpecific(CacheManager.Settings.LOCALE, key, args);
    }

    public static String getSpecific(final String locale, final String key) {
        return getSpecific(locale, key, Collections.emptyMap());
    }
    public static String getSpecific(final String locale, final String key, final Map<String, String> args) {
        Properties properties = LOCALES.get(locale);
        if (properties == null) properties = LOCALES.get(DEFAULT_LOCALE);

        String value = properties.getProperty(key);
        if (value == null) value = LOCALES.get(DEFAULT_LOCALE).getProperty(key);
        if (value == null) value = key;
        value = Utils.replaceParams(value);
        for (Map.Entry<String, String> e : args.entrySet()) value = value.replace(e.getKey(), e.getValue());
        return value;
    }

    public static void setLocale(final String locale) {
        CacheManager.Settings.LOCALE = locale;
    }

    public static Collection<String> getAvailableLocales() {
        return LOCALES.keySet().stream().sorted().toList();
    }

}
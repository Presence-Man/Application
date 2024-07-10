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

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import lombok.NonNull;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.entity.infos.XboxUserInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class CacheManager {
    private static final File CACHE_FILE = new File(System.getProperty("user.home")).toPath().resolve(".presence-man-cache.json").toFile();
    private static JsonObject json = new JsonObject();
    public static JsonObject settings = new JsonObject();

    static {
        load();
    }

    private static void load(){
        try {
            if (!CACHE_FILE.exists()) {
                CACHE_FILE.createNewFile();
                save();
            }
            FileReader reader = new FileReader(CACHE_FILE);
            json = Utils.GSON.fromJson(reader, JsonObject.class);
            settings = json.has("settings") && json.get("settings").isJsonObject() ? json.get("settings").getAsJsonObject() : new JsonObject();
            Settings.load();
            reader.close();
        } catch (IOException e) {
            App.getLogger().error(e);
            App.ui.showException(e);
        }
    }

    public static void save() {
        try {
            FileWriter writer = new FileWriter(CACHE_FILE);
            Settings.save();
            json.add("settings", settings);
            Utils.GSON.toJson(json, writer);
            writer.close();
        } catch (IOException e) {
            App.getLogger().error(e);
            App.ui.showException(e);
        }
    }

    public static XboxUserInfo loadXboxUserInfo() {
        if (!json.has("xuid") || json.get("xuid").isJsonNull()) return null;
        if (!json.has("gamertag") || json.get("gamertag").isJsonNull()) return null;
        return new XboxUserInfo(json.get("xuid").getAsString(), json.get("gamertag").getAsString());
    }

    public static void storeXboxUserInfo(XboxUserInfo xboxUserInfo) {
        json.addProperty("xuid", xboxUserInfo == null ? null : xboxUserInfo.getXuid());
        json.addProperty("gamertag", xboxUserInfo == null ? null : xboxUserInfo.getGamertag());
        save();
    }

    @NonNull
    public static JsonElement setting(String key){
        return settings.has(key) ? settings.get(key) : new JsonNull();
    }

    public static class Settings {
        public static boolean START_MINIMIZED = false;
        public static boolean ENABLE_AUTO_UPDATE = true;

        protected static void load() {
            if (!settings.has("start-minimized")) settings.addProperty("start-minimized", START_MINIMIZED);
            START_MINIMIZED = settings.get("start-minimized").getAsBoolean();
            if (!settings.has("enable-auto-update")) settings.addProperty("enable-auto-update", ENABLE_AUTO_UPDATE);
            ENABLE_AUTO_UPDATE = settings.get("enable-auto-update").getAsBoolean();
        }
        protected static void save() {
            settings.addProperty("start-minimized", START_MINIMIZED);
            settings.addProperty("enable-auto-update", ENABLE_AUTO_UPDATE);
        }
    }
}

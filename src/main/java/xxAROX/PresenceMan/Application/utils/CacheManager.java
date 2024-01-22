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
import com.google.gson.JsonObject;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.entity.XboxUserInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class CacheManager {
    private static final Gson GSON = new Gson();
    private static final File CACHE_FILE = new File(".presence-man-cache.json");
    private static JsonObject json = new JsonObject();

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
            json = GSON.fromJson(reader, JsonObject.class);
            reader.close();
        } catch (IOException e) {
            App.getLogger().error(e);
            App.ui.showException(e);
        }
    }

    public static void save() {
        try {
            FileWriter writer = new FileWriter(CACHE_FILE);
            GSON.toJson(json, writer);
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
}

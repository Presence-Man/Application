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
            App.getInstance().getLogger().error(e);
            App.ui.showException(e);
        }
    }

    public static void save() {
        try {
            FileWriter writer = new FileWriter(CACHE_FILE);
            GSON.toJson(json, writer);
            writer.close();
        } catch (IOException e) {
            App.getInstance().getLogger().error(e);
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

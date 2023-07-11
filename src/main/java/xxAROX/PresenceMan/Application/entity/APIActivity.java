package xxAROX.PresenceMan.Application.entity;

import com.google.gson.JsonObject;
import de.jcm.discordgamesdk.activity.Activity;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public final class APIActivity {
    private @NonNull ActivityType type = ActivityType.PLAYING;
    private String network;
    private String server;
    private Long start;
    private Long end;
    private String large_icon_key;
    private String large_icon_text;
    private String small_icon_key;
    private String small_icon_text;
    private Integer party_max_player_count;
    private Integer party_player_count;

    public JsonObject serialize(){
        JsonObject json = new JsonObject();
        json.addProperty("type", type.name().toUpperCase());
        json.addProperty("network", network);
        json.addProperty("server", server);
        json.addProperty("start", start);
        json.addProperty("end", end);
        json.addProperty("large_icon_key", large_icon_key);
        json.addProperty("large_icon_text", large_icon_text);
        json.addProperty("small_icon_key", small_icon_key);
        json.addProperty("small_icon_text", small_icon_text);
        json.addProperty("party_max_player_count", party_max_player_count);
        json.addProperty("party_player_count", party_player_count);
        return json;
    }

    public static APIActivity deserialize(JsonObject json){
        APIActivity activity = new APIActivity();
        activity.type = json.has("type") ? ActivityType.valueOf(json.get("type").getAsString()) : ActivityType.PLAYING;
        activity.network = json.has("network") ? json.get("network").getAsString() : null;
        activity.server = json.has("server") ? json.get("server").getAsString() : null;
        activity.start = json.has("start") ? json.get("start").getAsLong() : null;
        activity.end = json.has("end") ? json.get("end").getAsLong() : null;
        activity.large_icon_key = json.has("large_icon_key") ? json.get("large_icon_key").getAsString() : null;
        activity.large_icon_text = json.has("large_icon_text") ? json.get("large_icon_text").getAsString() : null;
        activity.small_icon_key = json.has("small_icon_key") ? json.get("small_icon_key").getAsString() : null;
        activity.small_icon_text = json.has("small_icon_text") ? json.get("small_icon_text").getAsString() : null;
        activity.party_max_player_count = json.has("party_max_player_count") ? json.get("party_max_player_count").getAsInt() : null;
        activity.party_player_count = json.has("party_player_count") ? json.get("party_player_count").getAsInt() : null;
        return activity;
    }

    public Activity toDiscord() {
        Activity activity = new Activity();
        activity.setType(type.toDiscordType());
        activity.setState(network == null ? "" : network);
        activity.setDetails(server == null ? "" : server);
        activity.timestamps().setStart(start == null ? null : Instant.ofEpochMilli(start));
        activity.timestamps().setEnd(end == null ? null : Instant.ofEpochMilli(end));
        activity.assets().setLargeImage(large_icon_key == null ? "" : large_icon_key);
        activity.assets().setLargeText(large_icon_text == null ? "" : large_icon_text);
        activity.assets().setSmallImage(small_icon_key == null ? "" : small_icon_key);
        activity.assets().setSmallText(small_icon_text == null ? "" : small_icon_text);
        activity.party().size().setCurrentSize(party_player_count);
        activity.party().size().setMaxSize(party_max_player_count);
        return activity;
    }

    @AllArgsConstructor
    public enum ActivityType {
        PLAYING("PLAYING"),
        STREAMING("STREAMING"),
        LISTENING("LISTENING"),
        UNUSED("UNUSED"),
        CUSTOM("CUSTOM"),
        COMPETING("COMPETING")
        ;
        private String raw;

        @Override
        public String toString() {
            return raw;
        }
        public de.jcm.discordgamesdk.activity.ActivityType toDiscordType(){
            return switch (raw) {
                case "STREAMING" -> de.jcm.discordgamesdk.activity.ActivityType.STREAMING;
                case "LISTENING" -> de.jcm.discordgamesdk.activity.ActivityType.LISTENING;
                case "UNUSED" -> de.jcm.discordgamesdk.activity.ActivityType.UNUSED;
                case "CUSTOM" -> de.jcm.discordgamesdk.activity.ActivityType.CUSTOM;
                case "COMPETING" -> de.jcm.discordgamesdk.activity.ActivityType.COMPETING;
                default -> de.jcm.discordgamesdk.activity.ActivityType.PLAYING;
            };
        }
    }

    public static APIActivity none() {
        var activity = new APIActivity();
        activity.setNetwork("");
        activity.setServer("");
        activity.setLarge_icon_key("bedrock");
        activity.setStart(Instant.now().toEpochMilli());
        return activity;
    }
}

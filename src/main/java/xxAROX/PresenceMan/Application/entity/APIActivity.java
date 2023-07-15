package xxAROX.PresenceMan.Application.entity;

import com.google.gson.JsonObject;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import lombok.*;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;

import java.time.Instant;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public final class APIActivity {
    private long client_id = AppInfo.discord_application_id;
    private @NonNull ActivityType type = ActivityType.PLAYING;
    private String state;
    private String details;
    private Long end;
    private String large_icon_key;
    private String large_icon_text;
    private String small_icon_key;
    private String small_icon_text;
    private Integer party_max_player_count;
    private Integer party_player_count;

    public JsonObject serialize(){
        JsonObject json = new JsonObject();
        json.addProperty("client_id", client_id);
        json.addProperty("type", type.name().toUpperCase());
        json.addProperty("state", state);
        json.addProperty("details", details);
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
        activity.client_id = json.has("client_id") ? json.get("client_id").getAsLong() : AppInfo.discord_application_id;
        activity.type = json.has("type") ? ActivityType.valueOf(json.get("type").getAsString()) : ActivityType.PLAYING;
        activity.state = json.has("state") ? json.get("state").getAsString() : null;
        activity.details = json.has("details") ? json.get("details").getAsString() : null;
        activity.end = json.has("end") ? json.get("end").getAsLong() : null;
        activity.large_icon_key = json.has("large_icon_key") ? json.get("large_icon_key").getAsString() : null;
        activity.large_icon_text = json.has("large_icon_text") ? json.get("large_icon_text").getAsString() : null;
        activity.small_icon_key = json.has("small_icon_key") ? json.get("small_icon_key").getAsString() : null;
        activity.small_icon_text = json.has("small_icon_text") ? json.get("small_icon_text").getAsString() : null;
        activity.party_max_player_count = json.has("party_max_player_count") ? json.get("party_max_player_count").getAsInt() : null;
        activity.party_player_count = json.has("party_player_count") ? json.get("party_player_count").getAsInt() : null;
        return activity;
    }

    public Activity toDiscord(CreateParams params) {
        if (params == null) return null;
        params.setClientID(client_id);
        Activity activity = new Activity();
        activity.setType(type.toDiscordType());
        activity.setState(state != null ? state : "");
        activity.setDetails(details != null ? details : "");
        if (end != null) activity.timestamps().setEnd(Instant.ofEpochMilli(end));
        if (large_icon_key != null) activity.assets().setLargeImage(large_icon_key);
        if (large_icon_text != null) activity.assets().setLargeText(large_icon_text);
        if (small_icon_key != null) activity.assets().setSmallImage(small_icon_key);
        if (small_icon_text != null) activity.assets().setSmallText(small_icon_text);
        if (party_player_count != null) activity.party().size().setCurrentSize(party_player_count);
        if (party_max_player_count != null) activity.party().size().setMaxSize(party_max_player_count);
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
        activity.setState("");
        activity.setDetails(App.getInstance().xboxUserInfo == null ? "" : "Playing as " + App.getInstance().xboxUserInfo.getGamertag());
        activity.setLarge_icon_key("launcher");
        return activity;
    }
}

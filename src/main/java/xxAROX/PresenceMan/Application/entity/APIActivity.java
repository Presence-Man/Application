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

package xxAROX.PresenceMan.Application.entity;

import com.google.gson.JsonObject;
import com.jagrosh.discordipc.entities.RichPresence;
import lombok.*;
import lombok.experimental.Accessors;
import net.arikia.dev.drpc.DiscordRichPresence;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;
import xxAROX.PresenceMan.Application.entity.enums.APITimestamp;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

@Getter @Setter @Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class APIActivity {
    private long client_id = AppInfo.discord_application_id;
    private @NonNull ActivityType type = ActivityType.PLAYING;
    private String state = null;
    private String details = null;
    private Long end = null;
    private APITimestamp start = null;
    private String large_icon_key = null;
    private String large_icon_text = null;
    private String small_icon_key = null;
    private String small_icon_text = null;
    private Integer party_max_player_count = null;
    private Integer party_player_count = null;

    public JsonObject serialize(){
        JsonObject json = new JsonObject();
        json.addProperty("client_id", client_id);
        json.addProperty("type", type.name().toUpperCase());
        json.addProperty("state", state);
        json.addProperty("details", details);
        json.addProperty("start", start == null ? null : start.getValue());
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
        activity.client_id = json.has("client_id") && !json.get("client_id").isJsonNull() ? json.get("client_id").getAsLong() : AppInfo.discord_application_id;
        activity.type = json.has("type") && !json.get("type").isJsonNull() ? ActivityType.valueOf(json.get("type").getAsString()) : ActivityType.PLAYING;
        activity.state = json.has("state") && !json.get("state").isJsonNull() ? json.get("state").getAsString() : null;
        activity.details = json.has("details") && !json.get("details").isJsonNull() ? json.get("details").getAsString() : null;
        activity.end = json.has("end") && !json.get("end").isJsonNull() ? json.get("end").getAsLong() : null;
        activity.large_icon_key = json.has("large_icon_key") && !json.get("large_icon_key").isJsonNull() ? json.get("large_icon_key").getAsString() : null;
        activity.large_icon_text = json.has("large_icon_text") && !json.get("large_icon_text").isJsonNull() ? json.get("large_icon_text").getAsString() : null;
        activity.small_icon_key = json.has("small_icon_key") && !json.get("small_icon_key").isJsonNull() ? json.get("small_icon_key").getAsString() : null;
        activity.small_icon_text = json.has("small_icon_text") && !json.get("small_icon_text").isJsonNull() ? json.get("small_icon_text").getAsString() : null;
        activity.party_max_player_count = json.has("party_max_player_count") && !json.get("party_max_player_count").isJsonNull() ? json.get("party_max_player_count").getAsInt() : null;
        activity.party_player_count = json.has("party_player_count") && !json.get("party_player_count").isJsonNull() ? json.get("party_player_count").getAsInt() : null;
        return activity;
    }

    public DiscordRichPresence toDiscord() {
        DiscordRichPresence.Builder activity = new DiscordRichPresence.Builder(state != null ? state : "");
        activity.setDetails(details != null ? details : "");
        if (start != null) {
            switch ((int) start.getValue()) {
                case (int) -1L -> start.setValue(App.getCreated());
                case (int) -2L -> start.setValue(App.network_session_created);
                case (int) -3L -> start.setValue(App.server_session_created);
            }
        } else {
            start = APITimestamp.CUSTOM;
            start.setValue(App.getCreated());
        }
        activity.setStartTimestamps(start.getValue());

        if (end != null) activity.setEndTimestamp(end);
        if (large_icon_key != null) activity.setBigImage((!Long.toString(client_id).equals(Long.toString(AppInfo.discord_application_id)) ? Gateway.getUrl()+"/i/" + client_id + "/" + large_icon_key : large_icon_key), large_icon_text);
        if (small_icon_key != null) activity.setSmallImage(small_icon_key, small_icon_text);
        if (party_player_count != null) activity.setParty("display", party_player_count, party_max_player_count);
        return activity.build();
    }

    public static RichPresence toRichPresence(DiscordRichPresence drpc) {
        RichPresence.Builder builder = new RichPresence.Builder();
        return builder
                .setState(drpc.state)
                .setDetails(drpc.details)
                .setStartTimestamp(Instant.ofEpochMilli(drpc.startTimestamp).atOffset(ZoneOffset.UTC))
                .setEndTimestamp(Instant.ofEpochMilli(drpc.endTimestamp).atOffset(ZoneOffset.UTC))
                .setLargeImage(drpc.largeImageKey, drpc.largeImageText)
                .setSmallImage(drpc.smallImageKey, drpc.smallImageText)
                .setParty(drpc.partyId, drpc.partySize, drpc.partyMax)
                .setMatchSecret(drpc.matchSecret)
                .setJoinSecret(drpc.joinSecret)
                .setSpectateSecret(drpc.spectateSecret)
                .build()
        ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        APIActivity activity = (APIActivity) o;
        return getClient_id() == activity.getClient_id()
                && getType() == activity.getType()
                && Objects.equals(getState(), activity.getState())
                && Objects.equals(getDetails(), activity.getDetails())
                && Objects.equals(getEnd(), activity.getEnd())
                && Objects.equals(getLarge_icon_key(), activity.getLarge_icon_key())
                && Objects.equals(getLarge_icon_text(), activity.getLarge_icon_text())
                && Objects.equals(getSmall_icon_key(), activity.getSmall_icon_key())
                && Objects.equals(getSmall_icon_text(), activity.getSmall_icon_text())
                && Objects.equals(getParty_max_player_count(), activity.getParty_max_player_count())
                && Objects.equals(getParty_player_count(), activity.getParty_player_count())
        ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClient_id(), getType(), getState(), getDetails(), getEnd(), getLarge_icon_key(), getLarge_icon_text(), getSmall_icon_key(), getSmall_icon_text(), getParty_max_player_count(), getParty_player_count());
    }

    @AllArgsConstructor
    @Deprecated
    public enum ActivityType {
        PLAYING("PLAYING"),
        STREAMING("STREAMING"),
        LISTENING("LISTENING"),
        UNUSED("UNUSED"),
        COMPETING("COMPETING")
        ;
        private String raw;

        @Override
        public String toString() {
            return raw;
        }
    }

    public static APIActivity none() {
        var app = App.getInstance();
        var activity = new APIActivity();
        activity.setState("");
        activity.setDetails("");
        activity.setLarge_icon_key("bedrock");
        activity.setLarge_icon_text(AppInfo.name + " - " + AppInfo.getVersion());
        activity.setSmall_icon_text(app == null || app.xboxUserInfo == null ? activity.getSmall_icon_text()  : App.getInstance().xboxUserInfo.getGamertag());
        activity.setSmall_icon_key(app == null || App.head_url == null ? activity.getSmall_icon_key()  : App.head_url);
        if (App.head_url == null) {
            AtomicInteger tries = new AtomicInteger(15);
            App.getInstance().getScheduler().scheduleRepeating(() -> {
                if (App.head_url != null) {
                    App.setActivity(activity);
                    throw new CancellationException();
                }
                tries.getAndDecrement();
                if (tries.get() == 0) throw new CancellationException();
            }, 20);
        }
        return activity;
    }
}

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

package xxAROX.PresenceMan.Application.sockets.protocol.packets.types;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import xxAROX.PresenceMan.Application.entity.APIActivity;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.CallbackPacket;

@Getter @Setter @ToString @NoArgsConstructor
public class HeartbeatPacket extends CallbackPacket {
    private String xuid = null; // required
    private String discord_user_id = null; // optional
    private String gamertag = null; // required
    private long sent = 0;
    // response
    private Integer network_id = null;
    private APIActivity api_activity = null;
    private String network = null;
    private String server = null;
    private Long received = null;
    private String head_url = null;

    @Override
    protected JsonObject encodeBody(JsonObject payload) {
        payload.addProperty("xuid", xuid);
        payload.addProperty("duid", discord_user_id);
        payload.addProperty("network_id", network_id);
        payload.addProperty("gamertag", gamertag);
        if (api_activity != null) payload.add("api_activity", api_activity.serialize());
        payload.addProperty("network", network);
        payload.addProperty("server", server);
        payload.addProperty("sent", sent);
        payload.addProperty("received", received);
        payload.addProperty("head_url", head_url);
        payload.addProperty("network_id", network_id);
        return super.encodeBody(payload);
    }

    @Override
    protected void decodeBody(JsonObject object) {
        xuid = object.get("xuid").getAsString();
        discord_user_id = object.has("duid") && !object.get("duid").isJsonNull() ? object.get("duid").getAsString() : discord_user_id;
        gamertag = object.has("gamertag") && !object.get("gamertag").isJsonNull() ? object.get("gamertag").getAsString() : gamertag;
        api_activity = object.has("api_activity") && object.get("api_activity").isJsonObject() ? APIActivity.deserialize(object.getAsJsonObject("api_activity")) : api_activity;
        network = object.has("network") && !object.get("network").isJsonNull() ? object.get("network").getAsString() : network;
        server = object.has("server") && !object.get("server").isJsonNull() ? object.get("server").getAsString() : server;
        sent = object.has("sent") && !object.get("sent").isJsonNull() ? object.get("sent").getAsLong() : sent;
        received = object.has("received") && !object.get("received").isJsonNull() ? object.get("received").getAsLong() : null;
        head_url = object.has("head_url") && !object.get("head_url").isJsonNull() ? object.get("head_url").getAsString() : null;
        network_id = object.has("network_id") && !object.get("network_id").isJsonNull() ? object.get("network_id").getAsInt() : null;
        super.decodeBody(object);
    }

    @Override
    public String getPacketType() {
        return heartbeat;
    }
}

package xxAROX.PresenceMan.Application.sockets.protocol.packets.types;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.CallbackPacket;

@Getter @Setter
public class HeartbeatPacket extends CallbackPacket {
    private String xuid = null;
    private String gamertag = null;
    private String user_id = null;

    @Override
    protected JsonObject encodeBody(JsonObject payload) {
        payload.addProperty("xuid", xuid);
        payload.addProperty("gamertag", gamertag);
        payload.addProperty("user_id", user_id);
        return super.encodeBody(payload);
    }

    @Override
    protected void decodeBody(JsonObject object) {
        xuid = object.has("xuid") && !object.get("xuid").isJsonNull() ? object.get("xuid").getAsString() : xuid;
        gamertag = object.has("gamertag") && !object.get("gamertag").isJsonNull() ? object.get("gamertag").getAsString() : gamertag;
        user_id = object.has("user_id") && !object.get("user_id").isJsonNull() ? object.get("user_id").getAsString() : user_id;
        super.decodeBody(object);
    }

    @Override
    public String getPacketType() {
        return heartbeat;
    }
}

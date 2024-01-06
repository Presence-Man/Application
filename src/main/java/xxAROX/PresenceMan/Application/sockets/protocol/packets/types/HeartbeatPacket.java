package xxAROX.PresenceMan.Application.sockets.protocol.packets.types;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import xxAROX.PresenceMan.Application.entity.APIActivity;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.CallbackPacket;

@Getter @Setter
public class HeartbeatPacket extends CallbackPacket {
    private APIActivity api_activity = null;
    private String network = null;
    private String server = null;
    private long sent = 0;
    private Long received = null;

    @Override
    protected JsonObject encodeBody(JsonObject payload) {
        if (api_activity != null) payload.add("api_activity", api_activity.serialize());
        payload.addProperty("network", network);
        payload.addProperty("server", server);
        payload.addProperty("sent", sent);
        payload.addProperty("received", received);
        return super.encodeBody(payload);
    }

    @Override
    protected void decodeBody(JsonObject object) {
        api_activity = object.has("api_activity") && object.get("api_activity").isJsonObject() ? APIActivity.deserialize(object.getAsJsonObject("sent")) : api_activity;
        network = object.has("network") && !object.get("network").isJsonNull() ? object.get("network").getAsString() : network;
        server = object.has("server") && !object.get("server").isJsonNull() ? object.get("server").getAsString() : server;
        sent = object.has("sent") && !object.get("sent").isJsonNull() ? object.get("sent").getAsLong() : sent;
        received = object.has("received") && !object.get("received").isJsonNull() ? object.get("received").getAsLong() : null;
        super.decodeBody(object);
    }

    @Override
    public String getPacketType() {
        return heartbeat;
    }
}

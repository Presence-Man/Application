package xxAROX.PresenceMan.Application.sockets.protocol.packets;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
abstract public class Packet extends PacketType {
    public String token = "";
    public JsonObject data = new JsonObject();

    public Packet() {
    }

    public final void decode(JsonObject json) {
        token = !json.has("token") || json.get("token").isJsonNull() ? "" : json.get("token").getAsString();
        data = json.get("data").getAsJsonObject();
        decodeBody(json);
    }
    public final JsonObject encode() {
        JsonObject json = new JsonObject();
        this.encodeBody(json);
        json.addProperty("type", getPacketType());
        json.addProperty("token", token);
        json.add("data", data);
        return json;
    }
    protected void decodeBody(JsonObject json) {
    }
    protected JsonObject encodeBody(JsonObject json) {
        return json;
    }

    public String getPacketType(){
        return PacketType.unknown;
    }
}

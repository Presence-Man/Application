package xxAROX.PresenceMan.Application.sockets.protocol.packets.types;

import com.google.gson.JsonObject;
import lombok.*;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.CallbackPacket;

@NoArgsConstructor @AllArgsConstructor
@ToString @Getter @Setter
public class HelloPacket extends CallbackPacket {
    private String xuid = null;
    private String discord_user_id = null;
    private String gamertag = null;

    // NOTE: Callback
    private String sToken = null;

    @Override
    protected JsonObject encodeBody(JsonObject json) {
        json.addProperty("xuid", xuid);
        json.addProperty("duid", discord_user_id);
        json.addProperty("gamertag", gamertag);
        json.addProperty("stoken", sToken);
        return super.encodeBody(json);
    }

    @Override
    protected void decodeBody(JsonObject json) {
        xuid = json.get("xuid").getAsString();
        discord_user_id = json.has("duid") && !json.get("duid").isJsonNull() ? json.get("duid").getAsString() : discord_user_id;
        gamertag = json.has("gamertag") && !json.get("gamertag").isJsonNull() ? json.get("gamertag").getAsString() : gamertag;
        sToken = json.has("stoken") && !json.get("stoken").isJsonNull() ? json.get("stoken").getAsString() : sToken;
        super.decodeBody(json);
    }

    @Override public String getPacketType() {
        return hello;
    }
}

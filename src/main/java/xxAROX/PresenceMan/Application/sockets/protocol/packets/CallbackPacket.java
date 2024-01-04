package xxAROX.PresenceMan.Application.sockets.protocol.packets;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
abstract public class CallbackPacket extends Packet {
    protected String callback_id = null;

    @Override
    protected JsonObject encodeBody(JsonObject json) {
        json.addProperty("callback_id", callback_id);
        return super.encodeBody(json);
    }

    @Override
    protected void decodeBody(JsonObject json) {
        callback_id = json.has("callback_id") ? json.get("callback_id").getAsString() : null;
        super.decodeBody(json);
    }

    public void error(Throwable throwable){
        data.addProperty("error", throwable.getMessage());
    }
}
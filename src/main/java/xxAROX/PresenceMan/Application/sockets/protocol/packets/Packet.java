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

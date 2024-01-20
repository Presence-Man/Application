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
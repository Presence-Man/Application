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

package xxAROX.PresenceMan.Application.sockets.protocol;

import lombok.NonNull;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.CallbackPacket;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public final class CallbackPacketManager {
    private static final String PREFIX = "app_";
    private static final HashMap<String, Consumer<CallbackPacket>> callbacks = new HashMap<>();
    public static void add(CallbackPacket packet, Consumer<CallbackPacket> callback){
        packet.setCallback_id(PREFIX + UUID.randomUUID());
        callbacks.put(packet.getCallback_id(), callback);
    }
    public static <CPacket extends CallbackPacket> CallbackPacket handle(@NonNull CPacket packet){
        try {
            if (packet.getCallback_id() == null) return null;
            if (!packet.getCallback_id().startsWith(PREFIX)) return packet;
            if (callbacks.containsKey(packet.getCallback_id())) {
                callbacks.remove(packet.getCallback_id()).accept(packet);
                App.getLogger().debug("Called!");
                return null;
            }
        } catch (Exception e) {
            App.getLogger().error("Error while executing callback for a " + packet.getPacketType() + " packet: {}", e);
            return null;
        }
        return packet;
    }
}

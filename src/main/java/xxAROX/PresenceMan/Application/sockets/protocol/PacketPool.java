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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.Packet;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.ByeByePacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.HeartbeatPacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.UnknownPacket;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class PacketPool {
    private static final Map<String, Class<? extends Packet>> packets = new HashMap<>();
    static {
        register(UnknownPacket.class);
        register(HeartbeatPacket.class);
        register(ByeByePacket.class);
    }
    private static void register(Class<? extends Packet> clazz) {
        try {
            Packet packet = clazz.getDeclaredConstructor().newInstance();
            if (packets.containsKey(packet.getPacketType())) throw new RuntimeException("[" + clazz.getSimpleName() + "] " + packet.getPacketType() + " is already registered in " + packets.get(packet.getPacketType()).getSimpleName() + "!");
            packets.put(packet.getPacketType(), clazz);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            App.getInstance().getLogger().error("Packet " + clazz.getSimpleName() + " is broken or has an non-empty constructor!");
        }
    }

    public static Packet decode(String payload) {
        try {
            JsonObject json = new Gson()
                    .newBuilder()
                    .setLenient()
                    .create().fromJson(payload, JsonObject.class);
            if (json == null || !json.has("type") || json.get("type").isJsonNull() || json.get("type").getAsString().isEmpty()) return new UnknownPacket();
            String type = json.get("type").getAsString().toLowerCase();
            Class<? extends Packet> clazz = packets.getOrDefault(type, UnknownPacket.class);
            Packet packet = clazz.getDeclaredConstructor().newInstance();
            packet.decode(json);
            return packet;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            App.getInstance().getLogger().error("Error while decoding packet: '" + payload + "'!");
            return new UnknownPacket();
        }
    }
}
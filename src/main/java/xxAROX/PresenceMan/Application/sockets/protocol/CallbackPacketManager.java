package xxAROX.PresenceMan.Application.sockets.protocol;

import lombok.NonNull;
import xxAROX.PresenceMan.Application.sockets.SocketThread;
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
                return null;
            }
        } catch (Exception e) {
            SocketThread.getInstance().getLogger().error("Error while executing callback for a {} packet: {}", packet.getPacketType(), e.getMessage());
            return null;
        }
        return packet;
    }
}

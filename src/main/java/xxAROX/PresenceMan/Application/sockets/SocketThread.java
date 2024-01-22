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

package xxAROX.PresenceMan.Application.sockets;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.NonNull;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.entity.Gateway;
import xxAROX.PresenceMan.Application.sockets.protocol.CallbackPacketManager;
import xxAROX.PresenceMan.Application.sockets.protocol.PacketPool;
import xxAROX.PresenceMan.Application.sockets.protocol.compressor.GzipCompressor;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.CallbackPacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.Packet;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.ByeByePacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.HeartbeatPacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.UnknownPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SocketThread implements Runnable {
    @Getter private final AtomicReference<String> session_token = new AtomicReference<>(null);
    @Getter private static SocketThread instance;
    @Getter private InetSocketAddress backend_address;
    @Getter private volatile boolean heartbeat_pending = false;
    @Getter private Socket socket;
    @Getter private final AtomicReference<State> connectionState = new AtomicReference<>(State.DISCONNECTED);
    private final Integer default_tries = 10;
    private final AtomicInteger tries_left = new AtomicInteger(default_tries);

    public SocketThread() {
        try {
            instance = this;
            backend_address = new InetSocketAddress(Gateway.ip, Gateway.usual_port +1);
            backend_address = new InetSocketAddress("127.0.0.1", Gateway.usual_port +1);
            App.getLogger().info("Backend identified as " + backend_address.getAddress().getHostAddress() + ":" + backend_address.getPort());
            socket = new Socket(this);
        } catch (Exception e) {
            App.getLogger().error("Error while creating socket: ", e);
        }
    }

    public void tick(int currentTick) {
        var xboxInfo = App.getInstance().getXboxUserInfo();
        if (xboxInfo == null) return;
        if (connectionState.get().equals(State.DISCONNECTED) && currentTick %(20*15) == 0) {
            if (tries_left.getAndDecrement() <= 0) {
                App.getLogger().error("Connection can't be established, shutting down..");
                shutdown();
            } else {
                connectionState.set(State.CONNECTING);
                App.getLogger().info(tries_left.get() +1 == default_tries ? "Connecting.." : "Reconnecting..");
                if (socket.connect()) {
                    connectionState.set(State.CONNECTED);
                    tries_left.set(default_tries);
                } else {
                    connectionState.set(State.DISCONNECTED);
                    App.getLogger().warn(tries_left.get() +1 == default_tries ? "Failed to connect to backend!" :"Reconnecting failed!");
                }
            }
        }
    }

    @Override public void run() {
        do {
            if (connectionState.get().equals(State.CONNECTED)) {
                String buffer = null;
                try {
                    buffer = socket.read();
                    System.out.println(buffer);
                } catch (Exception e) {
                    App.getLogger().error("Error while reading packet: ", e);
                }
                if (buffer != null && !buffer.isEmpty()) {
                    App.getLogger().info("Reading: " + buffer);
                    Packet packet = PacketPool.decode(buffer);
                    if (packet instanceof UnknownPacket) continue;
                    if (packet instanceof CallbackPacket callbackPacket && callbackPacket.getCallback_id() != null) {
                        CallbackPacket cbp = CallbackPacketManager.handle(callbackPacket);
                        if (cbp != null) sendPacket(cbp);
                    }
                }
            }
        } while (!connectionState.get().equals(State.SHUTDOWN));
        App.getLogger().debug("[SocketThread]: Bye!");
    }

    public void shutdown(){
        if (connectionState.get().equals(State.SHUTDOWN)) return;
        if (session_token.get() != null) {
            System.out.println(session_token);
            sendPacket(new ByeByePacket());
        }
        connectionState.set(State.SHUTDOWN);
        socket.close();
    }

    public <T extends Packet> boolean sendPacket(@NonNull T packet){
        if (connectionState.get().equals(State.SHUTDOWN) || connectionState.get().equals(State.DISCONNECTED)) return false;
        if (!connectionState.get().equals(State.CONNECTED) && packet instanceof HeartbeatPacket) return false;
        if (session_token.get() != null) packet.setToken(session_token.get());
        if (packet instanceof HeartbeatPacket heartbeatPacket) {
            heartbeatPacket.setSent(System.currentTimeMillis());
        }

        try {
            byte[] compressed = GzipCompressor.getInstance().compress(new Gson().toJson(packet.encode()));
            DatagramPacket pk = new DatagramPacket(compressed, compressed.length, backend_address.getAddress(), backend_address.getPort());
            try {
                socket.getSocket().send(pk);
                return true;
            } catch (IOException e) {
                App.getLogger().error("Error while sending packet to backend: ", e);
            }
        } catch (IOException e) {
            App.getLogger().error("Error while sending packet to backend: ", e);
        }
        return false;
    }
    public <T extends CallbackPacket> boolean sendPacket(@NonNull T packet, @NonNull Consumer<T> callback, Consumer<String> on_error){
        CallbackPacketManager.add(packet, (pk) -> {
            if (pk.data != null && pk.data.has("error") && !pk.data.get("error").isJsonNull()) {
                String error = pk.data.get("error").getAsString();
                App.getLogger().error("Error from backend: " + error);
                if (on_error != null) on_error.accept(error);
            } else callback.accept((T) pk);
        });
        return sendPacket(packet);
    }
    public <T extends CallbackPacket> boolean sendPacket(@NonNull T packet, @NonNull Consumer<T> callback){
        return sendPacket(packet, callback, null);
    }

    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        SHUTDOWN
    }
}

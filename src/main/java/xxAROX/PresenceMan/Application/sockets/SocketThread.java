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

import lombok.Getter;
import lombok.NonNull;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.entity.APIActivity;
import xxAROX.PresenceMan.Application.entity.Gateway;
import xxAROX.PresenceMan.Application.sockets.protocol.CallbackPacketManager;
import xxAROX.PresenceMan.Application.sockets.protocol.PacketPool;
import xxAROX.PresenceMan.Application.sockets.protocol.compressor.GzipCompressor;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.CallbackPacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.Packet;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.ByeByePacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.HeartbeatPacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.UnknownPacket;
import xxAROX.PresenceMan.Application.utils.Utils;

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
    @Getter private Socket socket;
    @Getter private final AtomicReference<State> connectionState = new AtomicReference<>(State.DISCONNECTED);

    @Getter private final AtomicInteger heartbeat_pending = new AtomicInteger(0);
    @Getter private boolean heartbeats_need_a_token = false;

    private final Integer default_tries = 10;
    private final AtomicInteger tries_left = new AtomicInteger(default_tries);

    public SocketThread() {
        try {
            instance = this;
            backend_address = new InetSocketAddress(Gateway.ip, Gateway.usual_port +1);
            App.getLogger().info("Backend socket located at " + backend_address.getAddress().getHostAddress() + ":" + backend_address.getPort());
            socket = new Socket(this);
        } catch (Exception e) {
            App.getLogger().error("Error while creating socket: ", e);
        }
    }

    public void heartbeat() {
        heartbeat(null);
    }

    public void heartbeat(Consumer<HeartbeatPacket> consumer){
        if (!connectionState.get().equals(SocketThread.State.CONNECTED)) return;
        if (session_token.get() == null && heartbeats_need_a_token) return;
        if (heartbeat_pending.get() == 10) {
            resetConnection();
            heartbeat_pending.set(0);
            return;
        }
        if (App.getInstance().xboxUserInfo == null) return;

        heartbeat_pending.getAndIncrement();

        var packet = new HeartbeatPacket();
        packet.setXuid(App.getInstance().xboxUserInfo.getXuid());
        packet.setGamertag(App.getInstance().xboxUserInfo.getGamertag());
        packet.setDiscord_user_id(App.getInstance().getDiscord_info().getId());
        App.getInstance().socket.sendPacket(packet, (pk) -> {
            if (consumer != null) consumer.accept(pk);
            if (!heartbeats_need_a_token && session_token.get() == null) {
                session_token.set(pk.getToken());
                heartbeats_need_a_token = true;
            }
            heartbeat_pending.getAndDecrement();
            App.head_url = pk.getHead_url();
            App.getInstance().network_info.network_id = pk.getNetwork_id();
            App.getInstance().updateServer(pk.getNetwork(), pk.getServer());
            APIActivity new_activity = pk.getApi_activity();
            if (new_activity == null) new_activity = APIActivity.none();
            if (new_activity.equals(App.getInstance().discord_info.getApi_activity())) return;
            App.setActivity(new_activity);
        }, err -> {
            App.head_url = null;
            App.getInstance().network_info.network = null;
            App.getInstance().network_info.server = null;
            App.getLogger().error("Error on heartbeat: " + err);
        });
    }

    public void resetConnection(){
        App.getLogger().info("Resetting connection..");
        connectionState.set(State.DISCONNECTED);
        heartbeats_need_a_token = false;
        session_token.set(null);
        wakeup();
    }

    public void tick(int currentTick) {
        var xboxInfo = App.getInstance().getXboxUserInfo();
        if (xboxInfo == null) return;
        if (connectionState.get().equals(State.DISCONNECTED) && currentTick %(20*5) == 0) {
            if (tries_left.getAndDecrement() <= 0) {
                App.getLogger().error("Connection can't be established, shutting down..");
                shutdown();
            } else {
                connectionState.set(State.CONNECTING);
                App.getLogger().info(tries_left.get() +1 == default_tries ? "Connecting.." : "Reconnecting..");
                if (socket.connect()) {
                    connectionState.set(State.CONNECTED);
                    App.getLogger().info(tries_left.get() +1 == default_tries ? "Connected!" : "Reconnected!");
                    tries_left.set(default_tries);
                    heartbeat();
                } else {
                    connectionState.set(State.DISCONNECTED);
                    App.getLogger().warn(tries_left.get() +1 == default_tries ? "Failed to connect to backend!" :"Reconnecting failed!");
                }
            }
        }
        if (currentTick %30 == 0) heartbeat();
    }

    @Override public void run() {
        do {
            if (connectionState.get().equals(State.CONNECTED)) {
                String buffer = null;
                try {
                    buffer = socket.read();
                } catch (Exception e) {
                    App.getLogger().error("Error while reading packet: ", e);
                }
                if (buffer != null && !buffer.isEmpty()) {
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
        if (session_token.get() != null && !session_token.get().equalsIgnoreCase("")) sendPacket(new ByeByePacket());
        connectionState.set(State.SHUTDOWN);
        socket.close();
    }

    public void wakeup(){
        if (!connectionState.get().equals(State.SHUTDOWN)) return;
        connectionState.set(State.DISCONNECTED);
        heartbeats_need_a_token = false;
        session_token.set(null);
        socket.connect();
    }

    public <T extends Packet> boolean sendPacket(@NonNull T packet){
        if (connectionState.get().equals(State.SHUTDOWN) || connectionState.get().equals(State.DISCONNECTED)) return false;
        if (!connectionState.get().equals(State.CONNECTED) && packet instanceof HeartbeatPacket) return false;
        if (session_token.get() != null) packet.setToken(session_token.get());
        if (packet instanceof HeartbeatPacket heartbeatPacket) {
            heartbeatPacket.setSent(System.currentTimeMillis());
        }

        try {
            byte[] compressed = GzipCompressor.getInstance().compress(Utils.GSON.toJson(packet.encode()));
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

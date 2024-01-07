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
    @Getter private volatile String session_token = null;
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
            System.out.println("Backend identified as " + backend_address.getAddress().getHostAddress() + ":" + backend_address.getPort());
            socket = new Socket(this);
            App.getInstance().getScheduler().scheduleRepeating(this::tick, 1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void tick() {
        var xboxInfo = App.getInstance().getXboxUserInfo();
        if (xboxInfo == null) return;
        if (connectionState.get().equals(State.DISCONNECTED) && App.getInstance().getCurrentTick() %(20*15) == 0) {
            if (tries_left.getAndDecrement() <= 0) {
                System.out.println("Connection can't be established, shutting down..");
                shutdown();
            } else {
                connectionState.set(State.CONNECTING);
                System.out.println(tries_left.get() +1 == default_tries ? "Connecting.." : "Reconnecting..");
                if (socket.connect()) {
                    connectionState.set(State.CONNECTED);
                    tries_left.set(default_tries);
                } else {
                    connectionState.set(State.DISCONNECTED);
                    System.out.println(tries_left.get() +1 == default_tries ? "Failed to connect to backend!" :"Reconnecting failed!");
                }
            }
        }
        else if (connectionState.get().equals(State.CONNECTED) && session_token == null) {
            heartbeat();
        }
    }

    public synchronized void heartbeat(){
        if (heartbeat_pending) return;
        heartbeat_pending = true;
        var xboxInfo = App.getInstance().getXboxUserInfo();
        HeartbeatPacket packet = new HeartbeatPacket();
        packet.setXuid(xboxInfo.getXuid());
        packet.setGamertag(xboxInfo.getGamertag());
        packet.setDiscord_user_id(App.getInstance().getDiscord_info().getDiscord_user_id());

        sendPacket(packet, (pk) -> {
            heartbeat_pending = false;
            System.out.println(pk);
            if (pk.getToken() != null) {
                session_token = pk.getToken();
                System.out.println("Successfully connected to backend!");
            }
        }, System.out::println);
    }

    @Override public void run() {
        do {
            if (connectionState.get().equals(State.CONNECTED)) {
                String buffer = null;
                try {
                    buffer = socket.read();
                } catch (Exception e) {
                    System.out.println("Error while reading packet:");
                    e.printStackTrace();
                }
                if (buffer != null && !buffer.isEmpty()) {
                    System.out.println("Reading: " + buffer);
                    Packet packet = PacketPool.decode(buffer);
                    if (packet instanceof UnknownPacket) continue;
                    if (packet instanceof CallbackPacket callbackPacket && callbackPacket.getCallback_id() != null) {
                        System.out.println(packet);
                        CallbackPacket cbp = CallbackPacketManager.handle(callbackPacket);
                        if (cbp != null) sendPacket(cbp);
                    }
                }
            }
        } while (!connectionState.get().equals(State.SHUTDOWN));
        System.out.println("[SocketThread]: bye!");
    }

    public void shutdown(){
        if (connectionState.get().equals(State.SHUTDOWN)) return;
        if (session_token != null) sendPacket(new ByeByePacket());
        connectionState.set(State.SHUTDOWN);
        socket.close();
    }

    public <T extends Packet> boolean sendPacket(@NonNull T packet){
        if (connectionState.get().equals(State.SHUTDOWN) || connectionState.get().equals(State.DISCONNECTED)) return false;
        if (!connectionState.get().equals(State.CONNECTED) && packet instanceof HeartbeatPacket) return false;
        packet.setToken(session_token);
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
                System.out.println("Error while sending packet to backend:");
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("Error while sending packet to backend:");
            e.printStackTrace();
        }

        return false;
    }
    public <T extends CallbackPacket> boolean sendPacket(@NonNull T packet, @NonNull Consumer<T> callback, Consumer<String> on_error){
        CallbackPacketManager.add(packet, (pk) -> {
            if (pk.data != null && pk.data.has("error") && !pk.data.get("error").isJsonNull()) {
                String error = pk.data.get("error").getAsString();
                System.out.println("Error from backend: " + error);
                if (on_error != null) on_error.accept(error);
            } else callback.accept((T) pk);
        });
        if (packet instanceof HeartbeatPacket) {
            System.out.println(Thread.currentThread().getName());
        }
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

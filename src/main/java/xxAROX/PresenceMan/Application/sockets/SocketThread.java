package xxAROX.PresenceMan.Application.sockets;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.NonNull;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.entity.Gateway;
import xxAROX.PresenceMan.Application.sockets.protocol.CallbackPacketManager;
import xxAROX.PresenceMan.Application.sockets.protocol.PacketPool;
import xxAROX.PresenceMan.Application.sockets.protocol.compressor.CompressorException;
import xxAROX.PresenceMan.Application.sockets.protocol.compressor.GzipCompressor;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.CallbackPacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.Packet;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.ByeByePacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.HeartbeatPacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.HelloPacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.UnknownPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SocketThread implements Runnable {
    private String session_token = null;
    @Getter private static SocketThread instance;
    @Getter private InetSocketAddress backend_address;
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
            new Thread(this).start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void tick() {
        var xboxInfo = App.getInstance().getXboxUserInfo();
        if (xboxInfo == null) return;
        if (connectionState.get().equals(State.DISCONNECTED) && App.getInstance().getCurrentTick() %(20*15) == 0) {
            if (tries_left.getAndDecrement() <= 0) {
                System.out.println("Connection can't be established, shutting down..");
                shutdown();
            } else {
                connectionState.set(State.CONNECTING);
                System.out.println(tries_left.get() == default_tries ? "Connecting.." : "Reconnecting..");
                if (socket.connect()) {
                    connectionState.set(State.CONNECTED);
                    tries_left.set(default_tries);
                    System.out.println(tries_left.get() == default_tries ? "Connected!" : "Reconnected!");
                } else {
                    connectionState.set(State.DISCONNECTED);
                    System.out.println(tries_left.get() == default_tries ? "Failed to connect to backend!" :"Reconnecting failed!");
                }
            }
        }
        else if (connectionState.get().equals(State.CONNECTED)) {
            System.out.println("Connecting..");
            connectionState.set(State.GREETING);
            HelloPacket packet = new HelloPacket(
                    xboxInfo.getXuid(),
                    App.getInstance().getDiscord_user_id(),
                    xboxInfo.getGamertag(),
                    null
            );
            sendPacket(packet, (pk) -> {
                if (pk.getSToken() != null) {
                    session_token = pk.getSToken();
                    System.out.println("Successfully connected to backend!");
                }
            }, System.out::println);
        }
    }

    @Override public void run() {
        do {
            if (connectionState.get().equals(State.CONNECTED)) {
                String buffer = null;
                try {
                    if (connectionState.get().equals(State.SHUTDOWN) || connectionState.get().equals(State.DISCONNECTED)) {
                        buffer = "";
                        continue;
                    }
                    byte[] bytes = new byte[65535];
                    DatagramPacket received = new DatagramPacket(bytes, bytes.length);
                    try {
                        socket.getSocket().receive(received);
                    } catch (IOException e) {
                       e.printStackTrace();
                    }
                    buffer =  GzipCompressor.getInstance().decompress(received.getData()).trim();
                } catch (CompressorException e) {
                    System.out.println("Error while decompressing packet:");
                    e.printStackTrace();
                }
                if (buffer != null && !buffer.isEmpty() && !buffer.isBlank()) {
                    Packet packet = PacketPool.decode(buffer);
                    if (packet instanceof UnknownPacket) continue;
                    if (packet instanceof CallbackPacket callbackPacket && callbackPacket.getCallback_id() != null) {
                        CallbackPacket cbp = CallbackPacketManager.handle(callbackPacket);
                        if (cbp != null) sendPacket(cbp);
                    }
                }
            }
        } while (!connectionState.get().equals(State.SHUTDOWN));
    }

    public void shutdown(){
        if (connectionState.get().equals(State.SHUTDOWN)) return;
        sendPacket(new ByeByePacket());
        connectionState.set(State.SHUTDOWN);
        socket.close();
    }

    public <T extends Packet> boolean sendPacket(@NonNull T packet){
        if (connectionState.get().equals(State.SHUTDOWN) || connectionState.get().equals(State.DISCONNECTED)) return false;
        if (!connectionState.get().equals(State.TRUSTED) && packet instanceof HeartbeatPacket) return false;
        if (packet instanceof HelloPacket && connectionState.get().equals(State.CONNECTED)) {
            connectionState.set(State.GREETING);
        }
        packet.setToken(session_token);
        if (packet instanceof HeartbeatPacket heartbeatPacket) heartbeatPacket.setSent(System.currentTimeMillis());
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
        return sendPacket(packet);
    }
    public <T extends CallbackPacket> boolean sendPacket(@NonNull T packet, @NonNull Consumer<T> callback){
        return sendPacket(packet, callback, null);
    }

    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        GREETING,
        TRUSTED,
        SHUTDOWN
    }
}

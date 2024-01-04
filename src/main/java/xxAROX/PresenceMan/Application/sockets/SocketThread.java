package xxAROX.PresenceMan.Application.sockets;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;
import xxAROX.PresenceMan.Application.sockets.protocol.CallbackPacketManager;
import xxAROX.PresenceMan.Application.sockets.protocol.PacketPool;
import xxAROX.PresenceMan.Application.sockets.protocol.compressor.CompressorException;
import xxAROX.PresenceMan.Application.sockets.protocol.compressor.GzipCompressor;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.CallbackPacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.Packet;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.HeartbeatPacket;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.UnknownPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SocketThread implements Runnable {
    @Getter private static SocketThread instance;
    @Getter private final Logger logger;
    @Getter private final InetSocketAddress cloud_address;
    @Getter private final Socket socket;
    private boolean authenticated;
    @Getter private final AtomicReference<State> connectionState = new AtomicReference<>(State.DISCONNECTED);
    private final Integer default_tries = 10;
    private final AtomicInteger tries_left = new AtomicInteger(default_tries);

    public SocketThread() {
        instance = this;
        logger = buildLogger();
        cloud_address = new InetSocketAddress(String.valueOf(System.getenv("CLOUD_ADDRESS")), Integer.parseInt(System.getenv("CLOUD_PORT")));
        logger.debug("Cloud identified as {}:{}", cloud_address.getAddress().getHostAddress(), cloud_address.getPort());
        socket = new Socket(this);
        App.getInstance().getScheduler().scheduleRepeating(this::tick, 1);
        new Thread(this).start();
    }

    private synchronized void tick() {
        if (connectionState.get().equals(State.DISCONNECTED) && App.getInstance().getCurrentTick() %(20*15) == 0) {
            if (tries_left.getAndDecrement() <= 0) {
                logger.info("Connection can't be established, shutting down..");
                shutdown();
            } else {
                synchronized (connectionState) {connectionState.set(State.CONNECTING);}connectionState.set(State.CONNECTING);
                if (socket.connect()) {
                    synchronized (connectionState) {connectionState.set(State.CONNECTED);}connectionState.set(State.CONNECTED);
                    tries_left.set(default_tries);
                    logger.info(tries_left.get() == default_tries ? "Connecting.." : "Reconnecting..");
                } else {
                    synchronized (connectionState) {connectionState.set(State.DISCONNECTED);}connectionState.set(State.DISCONNECTED);
                    logger.fatal(tries_left.get() == default_tries ? "Failed to connect to backend!" :"Reconnecting failed!");
                }
            }
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
                        logger.error(e);
                    }
                    buffer =  GzipCompressor.getInstance().decompress(received.getData()).trim();
                } catch (CompressorException e) {
                    logger.error(e);
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
        logger.info("Shutting down..");
        synchronized (connectionState) {connectionState.set(State.SHUTDOWN);}connectionState.set(State.SHUTDOWN);
        socket.close();
    }

    public <T extends Packet> boolean sendPacket(@NonNull T packet){
        if (connectionState.get().equals(State.SHUTDOWN) || connectionState.get().equals(State.DISCONNECTED)) return false;
        if (!connectionState.get().equals(State.AUTHENTICATED) && !(packet instanceof HeartbeatPacket)) return false;
        if (packet instanceof HeartbeatPacket && connectionState.get().equals(State.CONNECTED)) {
            synchronized (connectionState) {connectionState.set(State.AUTHENTICATING);}
            connectionState.set(State.AUTHENTICATING);
        }
        packet.setXuid(App.getInstance().getXboxUserInfo().getXuid());

        if (packet instanceof HeartbeatPacket heartbeatPacket) {
            heartbeatPacket.setGamertag(App.getInstance().getXboxUserInfo().getXuid());
            heartbeatPacket.setUser_id(App.getInstance().getDiscord_user_id());
        }
        try {
            byte[] compressed = GzipCompressor.getInstance().compress(new Gson().toJson(packet.encode()));
            DatagramPacket pk = new DatagramPacket(compressed, compressed.length, cloud_address.getAddress(), cloud_address.getPort());
            try {
                socket.getSocket().send(pk);
                return true;
            } catch (IOException e) {
                logger.error("Error while sending packet to backend:");
                logger.error(e);
            }
        } catch (IOException e) {
            logger.error(e);
        }

        return false;
    }
    public <T extends CallbackPacket> boolean sendPacket(@NonNull T packet, @NonNull Consumer<T> callback, Consumer<String> on_error){
        CallbackPacketManager.add(packet, (pk) -> {
            if (pk.data != null && pk.data.has("error") && !pk.data.get("error").isJsonNull()) {
                String error = pk.data.get("error").getAsString();
                logger.error("Error from cloud: {}", error);
                if (on_error != null) on_error.accept(error);
            } else callback.accept((T) pk);
        });
        return sendPacket(packet);
    }
    public <T extends CallbackPacket> boolean sendPacket(@NonNull T packet, @NonNull Consumer<T> callback){
        return sendPacket(packet, callback, null);
    }

    private Logger buildLogger() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = context.getConfiguration();
        Level logLevel = AppInfo.development ? Level.DEBUG : Level.INFO;
        AppenderRef[] appenderRefs = new AppenderRef[]{
                AppenderRef.createAppenderRef("File-Plugin", null, null),
                AppenderRef.createAppenderRef("Console-Plugin", logLevel, null)
        };
        LoggerConfig logger = LoggerConfig.createLogger(false, logLevel, this.getClass().getCanonicalName(), "", appenderRefs, null, config, null);
        logger.addAppender(config.getAppender("File-Plugin"), null, null);
        logger.addAppender(config.getAppender("Console-Plugin"), logLevel, null);
        config.addLogger(this.getClass().getCanonicalName(), logger);

        context.updateLoggers();
        return LogManager.getLogger(this.getClass().getCanonicalName());
    }

    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        AUTHENTICATING,
        AUTHENTICATED,
        SHUTDOWN
    }
}

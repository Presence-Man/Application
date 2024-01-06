package xxAROX.PresenceMan.Application.sockets;

import lombok.Getter;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.sockets.protocol.compressor.CompressorException;
import xxAROX.PresenceMan.Application.sockets.protocol.compressor.GzipCompressor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public final class Socket {
    private final SocketThread connection;
    @Getter private DatagramSocket socket = null;

    public Socket(SocketThread connection) {
        this.connection = connection;
    }

    public boolean connect(){
        close();
        try {
            DatagramSocket _socket = new DatagramSocket();
            _socket.connect(connection.getBackend_address().getAddress(), connection.getBackend_address().getPort());
            _socket.setReuseAddress(true);
            _socket.setReceiveBufferSize(65535);
            _socket.setSendBufferSize(65535);
            socket = _socket;
            return true;
        } catch (IOException e) {
            App.getInstance().getLogger().error("Error while connecting to cloud: {}", e.getMessage());
        }
        return false;
    }
    public String read() throws Exception {
        if (socket == null) return "";
        if (socket.isClosed() || !socket.isConnected()) throw new Exception("Socket is closed or not connected");
        String buffer = "";
        try {
            byte[] bytes = new byte[65535];
            DatagramPacket received = new DatagramPacket(bytes, bytes.length);
            try {
                socket.receive(received);
            } catch (IOException e) {
                e.printStackTrace();
            }
            buffer =  GzipCompressor.getInstance().decompress(received.getData()).trim();
        } catch (CompressorException e) {
            System.out.println("Error while decompressing packet:");
            e.printStackTrace();
        }
        return buffer;
    }

    public void close(){
        if (socket != null) {
            if (socket.isConnected()) socket.disconnect();
            if (socket.isClosed()) socket.close();
            socket = null;
        }
    }
}

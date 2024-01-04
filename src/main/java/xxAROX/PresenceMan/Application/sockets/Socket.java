package xxAROX.PresenceMan.Application.sockets;

import lombok.Getter;

import java.io.IOException;
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
            _socket.connect(connection.getCloud_address().getAddress(), connection.getCloud_address().getPort());
            _socket.setReuseAddress(true);
            _socket.setReceiveBufferSize(65535);
            _socket.setSendBufferSize(65535);
            socket = _socket;
            return true;
        } catch (IOException e) {
            connection.getLogger().error("Error while connecting to cloud: {}", e.getMessage());
        }
        return false;
    }

    public void close(){
        if (socket != null) {
            if (socket.isConnected()) socket.disconnect();
            if (socket.isClosed()) socket.close();
            socket = null;
        }
    }
}

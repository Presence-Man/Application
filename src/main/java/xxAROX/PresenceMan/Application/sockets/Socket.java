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
            App.getLogger().error("Error while connecting to cloud: {}", e);
        }
        return false;
    }
    public String read() throws Exception {
        if (socket == null) return null;
        if (socket.isClosed() || !socket.isConnected()) throw new Exception("Socket is closed or not connected");
        String buffer = null;
        try {
            byte[] bytes = new byte[65535];
            DatagramPacket received = new DatagramPacket(bytes, bytes.length);
            try {
                socket.receive(received);
            } catch (IOException e) {
                App.getLogger().error("Error when receiving data: ", e);
            }
            buffer = GzipCompressor.getInstance().decompress(received.getData()).trim();
        } catch (CompressorException e) {
            App.getLogger().error("Error while decompressing packet: ", e);
        }
        return (buffer == null || buffer.isEmpty() ? null : buffer);
    }

    public void close(){
        if (socket != null) {
            if (socket.isConnected()) socket.disconnect();
            if (socket.isClosed()) socket.close();
            socket = null;
        }
    }
}

package xxAROX.PresenceMan.Application.sockets.protocol.packets.types;

import xxAROX.PresenceMan.Application.sockets.protocol.packets.Packet;

public class UnknownPacket extends Packet {
    @Override
    public String getPacketType() {
        return unknown;
    }
}

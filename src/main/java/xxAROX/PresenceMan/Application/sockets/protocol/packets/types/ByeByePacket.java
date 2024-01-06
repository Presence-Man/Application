package xxAROX.PresenceMan.Application.sockets.protocol.packets.types;

import xxAROX.PresenceMan.Application.sockets.protocol.packets.Packet;

public class ByeByePacket extends Packet {
    @Override
    public String getPacketType() {
        return byebye;
    }
}

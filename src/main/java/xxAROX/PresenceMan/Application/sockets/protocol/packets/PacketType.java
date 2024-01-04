package xxAROX.PresenceMan.Application.sockets.protocol.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor @Getter @ToString
public class PacketType  {
    public static String unknown = "unknown";
    public static String heartbeat = "heartbeat";
    public static String disconnect = "disconnect";
}

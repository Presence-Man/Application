package xxAROX.PresenceMan.Application.sockets.protocol.compressor;

import java.io.IOException;

public class CompressorException extends IOException {
    public CompressorException(String message) {
        super(message);
    }
    public CompressorException(String message, Throwable cause) {
        super(message, cause);
    }
}

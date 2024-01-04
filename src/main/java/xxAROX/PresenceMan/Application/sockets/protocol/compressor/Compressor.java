package xxAROX.PresenceMan.Application.sockets.protocol.compressor;

import java.io.IOException;

public interface Compressor {
    public String decompress(byte[] payload) throws CompressorException;

    public byte[] compress(String payload) throws IOException;
}

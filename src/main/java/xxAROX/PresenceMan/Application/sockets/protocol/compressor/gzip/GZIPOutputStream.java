package xxAROX.PresenceMan.Application.sockets.protocol.compressor.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

public class GZIPOutputStream extends java.util.zip.GZIPOutputStream {
    public GZIPOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    public Deflater getDeflater(){
        return def;
    }
}

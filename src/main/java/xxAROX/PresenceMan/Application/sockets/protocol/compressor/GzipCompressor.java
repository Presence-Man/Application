package xxAROX.PresenceMan.Application.sockets.protocol.compressor;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xxAROX.PresenceMan.Application.sockets.protocol.compressor.gzip.GZIPOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

@Getter @Setter @Accessors(chain = true)
public final class GzipCompressor implements Compressor {
    private static int COMPRESSION_LEVEL = 6;
    @Getter private static GzipCompressor instance = new GzipCompressor();

    private GzipCompressor(){
    }

    public byte[] compress(String payload) throws CompressorException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            gzip.getDeflater().setLevel(payload.length() >= 256 ? COMPRESSION_LEVEL : 0);
            gzip.write(payload.getBytes(StandardCharsets.UTF_8));
            gzip.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new CompressorException("Failed to compress data", e);
        }
    }

    public String decompress(byte[] payload) throws CompressorException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(payload);
            GZIPInputStream gunzip = new GZIPInputStream(bais);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[65535];
            int len;
            while ((len = gunzip.read(buffer)) > 0) out.write(buffer, 0, len);
            gunzip.close();
            out.close();
            return out.toString();
        } catch (IOException e) {
            throw new CompressorException("Failed to decompress data", e);
        }
    }
}
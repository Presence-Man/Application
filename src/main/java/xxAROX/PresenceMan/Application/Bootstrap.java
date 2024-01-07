package xxAROX.PresenceMan.Application;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;

public class Bootstrap {
    @SneakyThrows
    public static void main(String[] args) {
        new App();
    }
    protected static void shutdownHook() {
        LogManager.shutdown();
        Runtime.getRuntime().halt(0); // force exit
    }
}

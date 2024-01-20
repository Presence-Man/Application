package xxAROX.PresenceMan.Application;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bootstrap {
    @SneakyThrows
    public static void main(String[] _args) {
        new App();
    }

    protected static void shutdownHook() {
        LogManager.shutdown();
        Runtime.getRuntime().halt(0); // force exit
    }
}

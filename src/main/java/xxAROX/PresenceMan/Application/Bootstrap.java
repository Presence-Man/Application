package xxAROX.PresenceMan.Application;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.GameSDKException;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

public class Bootstrap {
    @SneakyThrows
    public static void main(String[] args) {
        new App();
        new Thread(() -> {
            try {
                Core.initDownload();
                try (CreateParams params = new CreateParams()) {
                    params.setClientID(AppInfo.discord_application_id);
                    params.setFlags(CreateParams.getDefaultFlags());
                    try (Core core = new Core(params)) {
                        App.setDiscordCore(core);
                        while (true) {
                            core.runCallbacks();
                            try {
                                Thread.sleep(16);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (GameSDKException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    protected static void shutdownHook() {
        LogManager.shutdown();
        Runtime.getRuntime().halt(0); // force exit
    }
}

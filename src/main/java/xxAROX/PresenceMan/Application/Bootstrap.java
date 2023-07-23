package xxAROX.PresenceMan.Application;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.GameSDKException;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;

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
                        synchronized (params) {
                            App.setDiscordCore(params, core);
                        }
                        System.out.println("Discord is initialized!");
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
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).start();
    }
    protected static void shutdownHook() {
        LogManager.shutdown();
        Runtime.getRuntime().halt(0); // force exit
    }
}

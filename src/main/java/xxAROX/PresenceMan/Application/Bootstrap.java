package xxAROX.PresenceMan.Application;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.GameSDKException;
import xxAROX.PresenceMan.Application.utils.Activities;

import java.io.IOException;

public class Bootstrap {
    public static void main(String[] args) {
        new App();
        new Thread(() -> {
            try {
                Core.initDownload();
                try (CreateParams params = new CreateParams()) {
                    params.setClientID(1127704366565052526L);
                    params.setFlags(CreateParams.getDefaultFlags());
                    try (Core core = new Core(params)) {
                        App.getDiscordInitHandlers().add(c -> c.activityManager().updateActivity(Activities.none(), System.out::println));
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
}

package xxAROX.PresenceMan.Application.utils;

import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;

import java.time.Instant;

public final class Activities {
    public static Activity none(){
        Activity activity = new de.jcm.discordgamesdk.activity.Activity();
        activity.setDetails("");
        activity.setState("");
        activity.setType(ActivityType.PLAYING);
        activity.assets().setLargeImage("bedrock");

        activity.timestamps().setStart(Instant.now());

        return activity;
    }
}

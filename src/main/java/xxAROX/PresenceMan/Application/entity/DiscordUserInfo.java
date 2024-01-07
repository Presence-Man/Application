package xxAROX.PresenceMan.Application.entity;
/*
import io.github.deathbeam.discordgamesdk.extensions.*;
import io.github.deathbeam.discordgamesdk.jna.DiscordActivity;
import io.github.deathbeam.discordgamesdk.jna.IDiscordActivityManager;
import io.github.deathbeam.discordgamesdk.jna.IDiscordCore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;

@Deprecated
@ToString @Getter @Setter @Slf4j
@ExtensionMethod({
        DiscordCoreExtensions.class,
        DiscordActivityExtensions.class,
        DiscordActivityManagerExtensions.class,
        DiscordUserExtensions.class,
        DiscordUserManagerExtensions.class
})
public final class DiscordUserInfo {
    private static DiscordUserInfo instance;
    public static DiscordUserInfo getInstance(){return instance;}
    public static IDiscordCore core;

    public Long current_client_id = AppInfo.discord_application_id;

    public DiscordUserInfo(IDiscordCore core) {
        DiscordUserInfo.core = core;
        if (instance != null) return;
        instance = this;
    }
    public static void setActivity(APIActivity api_activity) {
        setActivity(api_activity, true);
    }
    public static void setActivity(APIActivity api_activity, boolean queue) {
        IDiscordActivityManager activityManager = core.getActivityManager();
        if (api_activity == null) api_activity = APIActivity.none();
        App app = App.getInstance();
        if (api_activity.equals(app.api_activity)) return;
        app.api_activity = api_activity;
        if (app.xboxUserInfo != null) {
            if (api_activity.getState() != null) api_activity.setState(replace(api_activity.getState()));
            if (api_activity.getDetails() != null) api_activity.setDetails(replace(api_activity.getDetails()));
            if (api_activity.getLarge_icon_key() == null || api_activity.getLarge_icon_key().isBlank()) api_activity.setLarge_icon_key("bedrock");
            if (api_activity.getLarge_icon_text() != null && !api_activity.getLarge_icon_text().isBlank()) api_activity.setLarge_icon_text(replace(api_activity.getLarge_icon_text()));
        }
        if (core != null) {
            var activity = api_activity.toDiscord(App.discord);
            discord_core.getActivityManager(activity);
        } else {
            if (queue) {
                APIActivity finalApi_activity = api_activity;
                discordInitHandlers.add(core -> setActivity(finalApi_activity, false));
            } else System.out.println("Discord is not initialized!");
        }
    }

    public static String replace(String input){
        return input
                .replace("{network}", App.getInstance().network == null ? "null" : App.getInstance().network)
                .replace("{server}", App.getInstance().server == null ? "null" : App.getInstance().server)
                .replace("{xuid}", App.getInstance().xboxUserInfo.getXuid())
                .replace("{gamertag}", App.getInstance().xboxUserInfo.getGamertag())
                .replace("{App.name}", AppInfo.name)
                .replace("{App.version}", AppInfo.getVersion())
                ;
    }
}
*/
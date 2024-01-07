package xxAROX.PresenceMan.Application;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xxAROX.PresenceMan.Application.entity.APIActivity;
import xxAROX.PresenceMan.Application.entity.DiscordInfo;
import xxAROX.PresenceMan.Application.entity.FeaturedServer;
import xxAROX.PresenceMan.Application.entity.XboxUserInfo;
import xxAROX.PresenceMan.Application.scheduler.WaterdogScheduler;
import xxAROX.PresenceMan.Application.sockets.SocketThread;
import xxAROX.PresenceMan.Application.task.FetchGatewayInformationTask;
import xxAROX.PresenceMan.Application.task.UpdateCheckTask;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.utils.CacheManager;
import xxAROX.PresenceMan.Application.utils.ThreadFactoryBuilder;
import xxAROX.PresenceMan.Application.utils.Tray;

import javax.swing.*;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Getter
@ToString
public final class App {
    @Getter static final long created = Instant.now().toEpochMilli();
    public static Long network_session_created = null;
    public static Long server_session_created = null;

    @Getter private DiscordInfo discord_info = new DiscordInfo();

    private static App instance;
    public SocketThread socket = null;
    public String network = null;
    public String server = null;

    public static App getInstance() {
        return instance;
    }

    private final Logger logger = LogManager.getLogger(AppInfo.name);
    public static AppUI ui;
    private WaterdogScheduler scheduler;
    private ScheduledExecutorService tickExecutor;
    private ScheduledFuture<?> tickFuture;
    private volatile boolean shutdown = false;
    private int currentTick = 0;
    public APIActivity api_activity = null;
    public XboxUserInfo xboxUserInfo = null;
    public FeaturedServer featuredServer = null;

    @SneakyThrows
    public App() {
        if (instance != null) {
            System.exit(0);
            return;
        }
        instance = this;
        Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC::discordShutdown));
        initDiscord();

        ThreadFactoryBuilder builder = ThreadFactoryBuilder.builder().format("Tick Executor - #%d").build();
        tickExecutor = Executors.newScheduledThreadPool(1, builder);

        scheduler = new WaterdogScheduler();
        tickFuture = tickExecutor.scheduleAtFixedRate(this::tickProcessor, 50, 50, TimeUnit.MILLISECONDS);

        SwingUtilities.invokeLater(() -> ui = new AppUI());
        scheduler.scheduleAsync(new UpdateCheckTask());
        App.getInstance().getScheduler().scheduleAsync(new FetchGatewayInformationTask());

        xboxUserInfo = CacheManager.loadXboxUserInfo();
        if (xboxUserInfo != null) discord_info.registerHandler(() -> App.getInstance().onLogin());

        while (ui == null) {
            logger.info("Waiting for UI to be initialized..");
            Thread.sleep(1000);
        }
        ui.setReady();
        new Tray();
    }

    private void tickProcessor() {
        if (shutdown && !tickFuture.isCancelled()) tickFuture.cancel(false);
        try {
            onTick(++currentTick);
        } catch (Exception e) {
            logger.error("Error while ticking application!", e);
        }
    }

    private void onTick(int currentTick) {
        scheduler.scheduleAsync(DiscordRPC::discordRunCallbacks);
        scheduler.onTick(currentTick);
    }

    public void shutdown() {
        if (shutdown) return;
        shutdown = true;

        try {
            shutdown0();
        } catch (Exception e) {
            logger.error("Unable to shutdown app gracefully", e);
        } finally {
            DiscordRPC.discordShutdown();
            Bootstrap.shutdownHook();
        }
    }
    private void shutdown0() throws Exception {
        Thread.sleep(500);

        tickExecutor.shutdown();
        scheduler.shutdown();

        if (!tickFuture.isCancelled()) {
            logger.info("Interrupting scheduler!");
            tickFuture.cancel(true);
        }
        logger.info("Shutdown complete!");
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

    public void onLogin() {
        setActivity(APIActivity.none());
    }

    public void onLogout() {
        CacheManager.storeXboxUserInfo(null);
        xboxUserInfo = null;
        setActivity(APIActivity.none());
    }

    public void initSocket() {
        if (socket == null) socket = new SocketThread();
    }

    public void initDiscord(){
        initDiscord(String.valueOf(AppInfo.discord_application_id));
    }
    public synchronized void initDiscord(String application_id) {
        if (discord_info == null) discord_info = new DiscordInfo();
        if (discord_info.getCurrent_application_id().equals(application_id)) return;
        if (discord_info.getDiscord_user_id() != null) DiscordRPC.discordShutdown();
        discord_info.setCurrent_application_id(application_id);
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                .setReadyEventHandler((user) -> {
                    var discord_info = App.getInstance().discord_info;
                    discord_info.setDiscord_user_id(user.userId);
                    discord_info.ready = true;
                    discord_info.checkHandlers();
                    System.out.println("Welcome @" + user.username + ", discord is ready!");
                })
                .build()
        ;
        DiscordRPC.discordInitialize(discord_info.getCurrent_application_id(), handlers, false);
        DiscordRPC.discordRegister(discord_info.getCurrent_application_id(), "");
        setActivity(APIActivity.none());
    }

    public static void setActivity(APIActivity api_activity) {
        setActivity(api_activity, true);
    }
    public static void setActivity(APIActivity api_activity, boolean queue) {
        App app = App.getInstance();
        if (api_activity == null) api_activity = APIActivity.none();
        if (api_activity.equals(app.api_activity)) return;
        app.api_activity = api_activity;
        if (app.xboxUserInfo != null) {
            if (api_activity.getState() != null) api_activity.setState(replace(api_activity.getState()));
            if (api_activity.getDetails() != null) api_activity.setDetails(replace(api_activity.getDetails()));
            if (api_activity.getLarge_icon_key() == null || api_activity.getLarge_icon_key().isBlank()) api_activity.setLarge_icon_key("bedrock");
            if (api_activity.getLarge_icon_text() != null && !api_activity.getLarge_icon_text().isBlank()) api_activity.setLarge_icon_text(replace(api_activity.getLarge_icon_text()));
        }
        App.getInstance().initDiscord(String.valueOf(api_activity.getClient_id()));
        if (app.discord_info.ready) DiscordRPC.discordUpdatePresence(api_activity.toDiscord());
        else if (queue) {
            APIActivity finalApi_activity = api_activity;
            app.discord_info.registerHandler(() -> setActivity(finalApi_activity, false));
        }
    }
}

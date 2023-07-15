package xxAROX.PresenceMan.Application;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.GameSDKException;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xxAROX.PresenceMan.Application.entity.APIActivity;
import xxAROX.PresenceMan.Application.entity.XboxUserInfo;
import xxAROX.PresenceMan.Application.scheduler.WaterdogScheduler;
import xxAROX.PresenceMan.Application.task.UpdateCheckTask;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.utils.CacheManager;
import xxAROX.PresenceMan.Application.utils.ThreadFactoryBuilder;
import xxAROX.PresenceMan.Application.utils.Tray;

import javax.swing.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
@ToString
public final class App {
    @Getter static final long created = Instant.now().toEpochMilli();
    @Getter
    private static CreateParams discord_create_params;
    @Getter
    private static Core discord_core;
    @Getter
    private static final List<Consumer<Core>> discordInitHandlers = new ArrayList<>();

    private static App instance;
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
    private APIActivity api_activity = null;
    public XboxUserInfo xboxUserInfo = null;

    @SneakyThrows
    public App() {
        if (instance != null) {
            System.exit(0);
            return;
        }
        instance = this;

        ThreadFactoryBuilder builder = ThreadFactoryBuilder.builder().format("Tick Executor - #%d").build();
        tickExecutor = Executors.newScheduledThreadPool(1, builder);
        scheduler = new WaterdogScheduler();
        tickFuture = tickExecutor.scheduleAtFixedRate(this::tickProcessor, 50, 50, TimeUnit.MILLISECONDS);

        SwingUtilities.invokeLater(() -> ui = new AppUI());
        scheduler.scheduleAsync(new UpdateCheckTask());

        xboxUserInfo = CacheManager.loadXboxUserInfo();
        if (xboxUserInfo != null) discordInitHandlers.add(core -> App.getInstance().onLogin());

        while (ui == null) {
            logger.info("Waiting for UI to be initialized..");
            Thread.sleep(1000);
        }
        ui.setReady();
        new Tray();

        scheduler.scheduleRepeating(RestAPI::heartbeat, 20 * 5);
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

    public static void setDiscordApplicationId(long id) {
        if (discord_core != null) {
            discord_core.close();
            discord_core = null;
        }
        if (discord_create_params != null) {
            discord_create_params.close();
            discord_create_params = null;
        }

        try (CreateParams params = new CreateParams()) {
            discord_create_params = params;
            params.setClientID(id);
            params.setFlags(CreateParams.getDefaultFlags());
            try (Core core = new Core(params)) {
                discord_core = core;
            } catch (GameSDKException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setDiscordCore(CreateParams discord_create_params, Core discord_core) {
        App.discord_create_params = discord_create_params;
        App.discord_core = discord_core;
        setActivity(APIActivity.none());
        for (Consumer<Core> discord_core_listener : discordInitHandlers) discord_core_listener.accept(discord_core);
    }

    public static void setActivity(APIActivity api_activity) {
        if (api_activity == null) api_activity = APIActivity.none();
        if (Objects.equals(getInstance().api_activity, api_activity)) return;
        api_activity.setStart(created);
        getInstance().api_activity = api_activity;
        if (discord_core != null) discord_core.activityManager().updateActivity(api_activity.toDiscord(discord_create_params));
    }

    public static void clearActivity() {
        getInstance().api_activity = null;
        discord_core.activityManager().clearActivity();
    }

    public void onLogin() {
        setActivity(APIActivity.none());
    }

    public void onLogout() {
        CacheManager.storeXboxUserInfo(null);
        xboxUserInfo = null;
        setActivity(APIActivity.none());
    }
}

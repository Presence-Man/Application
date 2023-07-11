package xxAROX.PresenceMan.Application;

import de.jcm.discordgamesdk.Core;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xxAROX.PresenceMan.Application.entity.APIActivity;
import xxAROX.PresenceMan.Application.entity.XboxUserInfo;
import xxAROX.PresenceMan.Application.scheduler.WaterdogScheduler;
import xxAROX.PresenceMan.Application.task.RestAPI;
import xxAROX.PresenceMan.Application.task.UpdateCheckTask;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.utils.CacheManager;
import xxAROX.PresenceMan.Application.utils.ThreadFactoryBuilder;
import xxAROX.PresenceMan.Application.utils.Tray;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
@ToString
public final class App {
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

    @Setter private XboxUserInfo xboxUserInfo = null;

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
        tickFuture = tickExecutor.scheduleAtFixedRate(this::tickProcessor, 50, 50, TimeUnit.MILLISECONDS);

        scheduler.scheduleRepeating(() -> {

            if (xboxUserInfo != null) RestAPI.heartbeat();
        }, 20 * 5);
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

    public void onLogin() {
        var base = APIActivity.none();
        base.setServer("Logged in as: " + xboxUserInfo.getGamertag());
        discord_core.activityManager().updateActivity(base.toDiscord());
    }

    public void onLogout() {
        discord_core.activityManager().updateActivity(APIActivity.none().toDiscord());
    }

    public static void setDiscordCore(Core discord_core) {
        App.discord_core = discord_core;
        for (Consumer<Core> discord_core_listener : discordInitHandlers) discord_core_listener.accept(discord_core);
        discord_core.activityManager().updateActivity(APIActivity.none().toDiscord());
    }
}

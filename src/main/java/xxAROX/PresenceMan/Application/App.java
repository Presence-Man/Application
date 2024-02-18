/*
 * Copyright (c) 2024. By Jan-Michael Sohn also known as @xxAROX.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xxAROX.PresenceMan.Application;

import lombok.AllArgsConstructor;
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
import xxAROX.PresenceMan.Application.events.IBaseListener;
import xxAROX.PresenceMan.Application.scheduler.WaterdogScheduler;
import xxAROX.PresenceMan.Application.sockets.SocketThread;
import xxAROX.PresenceMan.Application.task.FetchGatewayInformationTask;
import xxAROX.PresenceMan.Application.task.UpdateCheckTask;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.utils.CacheManager;
import xxAROX.PresenceMan.Application.utils.ThreadFactoryBuilder;
import xxAROX.PresenceMan.Application.utils.Tray;
import xxAROX.PresenceMan.Application.utils.Utils;

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
    public static String head_url = null;

    @Getter private DiscordInfo discord_info = new DiscordInfo();

    private static App instance;
    @Getter private static App.Events events;
    public SocketThread socket = null;
    public String network = null;
    public String server = null;

    public static App getInstance() {
        return instance;
    }

    private Logger logger;
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
    public App(Logger logger) {
        if (instance != null) {
            System.exit(0);
            return;
        }
        this.logger = logger;
        instance = this;
        events = new Events(this);

        Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC::discordShutdown));
        initDiscord();

        ThreadFactoryBuilder builder = ThreadFactoryBuilder.builder().format("Tick Executor - #%d").build();
        tickExecutor = Executors.newScheduledThreadPool(1, builder);

        scheduler = new WaterdogScheduler();
        tickFuture = tickExecutor.scheduleAtFixedRate(this::processTick, 50, 50, TimeUnit.MILLISECONDS);

        SwingUtilities.invokeLater(() -> ui = new AppUI());
        scheduler.scheduleAsync(new UpdateCheckTask());
        App.getInstance().getScheduler().scheduleAsync(new FetchGatewayInformationTask());

        xboxUserInfo = CacheManager.loadXboxUserInfo();
        logger.info("App is in " + (AppInfo.development ? "development" : "production") + " mode");

        while (ui == null) {Thread.sleep(1000);}
        ui.setReady();
        new Tray();
    }

    private void tick(int currentTick) {
        scheduler.scheduleAsync(DiscordRPC::discordRunCallbacks);
        if (socket != null) socket.tick(currentTick);
        if (App.ui != null) App.ui.general_tab.tick();
        scheduler.onTick(currentTick);
    }

    private void shutdownServices() {
        tickExecutor.shutdown();
        scheduler.shutdown();
        if (socket != null) socket.shutdown();
    }

    public void updateServer(String new_network, String new_server) {
        String before_network = network;
        if (before_network == null || !before_network.equalsIgnoreCase(new_network)) {
            network_session_created = new_network == null ? null : Instant.now().toEpochMilli();
            network = new_network;
        }
        String before_server = server;
        if (before_server == null || !before_server.equalsIgnoreCase(new_server)) {
            server_session_created = new_server == null ? null : Instant.now().toEpochMilli();
            server = new_server;
        }
    }

    public void shutdown() {
        if (shutdown) return;
        shutdown = true;

        try {
            Thread.sleep(500);
            shutdownServices();
            if (!tickFuture.isCancelled()) {
                logger.info("Interrupting scheduler!");
                tickFuture.cancel(true);
            }
            logger.info("Shutdown complete!");
        } catch (Exception e) {
            logger.error("Unable to shutdown application gracefully", e);
        } finally {
            DiscordRPC.discordShutdown();
            LogManager.shutdown();
            Runtime.getRuntime().halt(0); // force exit
        }
    }

    public void initSocket() {
        if (socket == null) {
            socket = new SocketThread();
            scheduler.scheduleAsync(socket);
        }
    }

    public void initDiscord(){
        initDiscord(String.valueOf(AppInfo.discord_application_id));
    }
    public synchronized void initDiscord(String application_id) {
        if (discord_info == null) discord_info = new DiscordInfo();
        if (discord_info.getCurrent_application_id().equals(application_id)) return;

        discord_info.setCurrent_application_id(application_id);
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                .setReadyEventHandler((user) -> {
                    var discord_info = App.getInstance().discord_info;
                    discord_info.setId(user.userId);
                    discord_info.setUsername(user.username);
                    discord_info.ready = true;
                    discord_info.checkHandlers();
                    events.onDiscordReady(discord_info);
                })
                .build()
        ;
        DiscordRPC.discordInitialize(discord_info.getCurrent_application_id(), handlers, false);
        DiscordRPC.discordRegister(discord_info.getCurrent_application_id(), "");
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
            if (api_activity.getState() != null) api_activity.setState(Utils.replaceParams(api_activity.getState()));
            if (api_activity.getDetails() != null) api_activity.setDetails(Utils.replaceParams(api_activity.getDetails()));
            if (api_activity.getLarge_icon_key() == null || api_activity.getLarge_icon_key().isBlank()) api_activity.setLarge_icon_key("bedrock");
            if (api_activity.getLarge_icon_text() != null && !api_activity.getLarge_icon_text().isBlank()) api_activity.setLarge_icon_text(Utils.replaceParams(api_activity.getLarge_icon_text()));
            if (App.head_url != null) {
                api_activity.setSmall_icon_key(App.head_url);
                if (api_activity.getSmall_icon_text() == null) api_activity.setSmall_icon_text(app.xboxUserInfo.getGamertag());
            }
        }
        App.getInstance().initDiscord(String.valueOf(api_activity.getClient_id()));
        APIActivity finalApi_activity1 = api_activity;
        App.getInstance().discord_info.registerHandler(() -> {
            if (app.discord_info.ready) {
                events.onDiscordActivityUpdate(finalApi_activity1);
                DiscordRPC.discordUpdatePresence(finalApi_activity1.toDiscord());
            } else if (queue) app.discord_info.registerHandler(() -> setActivity(finalApi_activity1, false));
        });
    }

    public static Logger getLogger(){
        return instance.logger;
    }

    private void processTick() {
        if (shutdown && !tickFuture.isCancelled()) tickFuture.cancel(false);
        try {
            tick(++currentTick);
        } catch (Exception e) {
            logger.error("Error while ticking application!", e);
        }
    }

    @AllArgsConstructor
    public final class Events implements IBaseListener {
        private App app;

        @Override
        public void onLogin(XboxUserInfo info) {
            xboxUserInfo = info;
            CacheManager.storeXboxUserInfo(info);
        }

        @Override
        public void onLogout() {
            xboxUserInfo = null;
            CacheManager.storeXboxUserInfo(null);
            setActivity(APIActivity.none());
        }

        @Override
        public void onDiscordReady(DiscordInfo info) {
            logger.info("Welcome @" + info.getUsername() + ", discord is ready!");
            setActivity(APIActivity.none());
        }
    }
}

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

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xxAROX.PresenceMan.Application.entity.APIActivity;
import xxAROX.PresenceMan.Application.entity.Gateway;
import xxAROX.PresenceMan.Application.entity.infos.DiscordInfo;
import xxAROX.PresenceMan.Application.entity.infos.NetworkInfo;
import xxAROX.PresenceMan.Application.entity.infos.XboxUserInfo;
import xxAROX.PresenceMan.Application.events.IBaseListener;
import xxAROX.PresenceMan.Application.scheduler.WaterdogScheduler;
import xxAROX.PresenceMan.Application.sockets.SocketThread;
import xxAROX.PresenceMan.Application.task.FetchGatewayInformationTask;
import xxAROX.PresenceMan.Application.task.UpdateCheckTask;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.utils.CacheManager;
import xxAROX.PresenceMan.Application.utils.ThreadFactoryBuilder;
import xxAROX.PresenceMan.Application.utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Getter
@ToString
public final class App {
    private static App instance;

    public static App getInstance() {
        return instance;
    }
    public static String head_url = null;

    @Getter private static App.Events events;
    @Getter static final long created = Instant.now().toEpochMilli();
    private Logger logger;
    public static AppUI ui;
    public SocketThread socket = null;
    private WaterdogScheduler scheduler;
    private ScheduledExecutorService tickExecutor;
    private ScheduledFuture<?> tickFuture;
    private int currentTick = 0;
    private volatile boolean shutdown = false;

    public NetworkInfo network_info = new NetworkInfo();
    public DiscordInfo discord_info = new DiscordInfo();
    public XboxUserInfo xboxUserInfo = null;

    private ImageIcon transparent_banner = null; // Cache

    public boolean isConnectedViaMCBE() {
        return App.getInstance().network_info.network_id != null;
    }

    @SneakyThrows
    public App(Logger logger) {
        if (instance != null) {
            System.exit(0);
            return;
        }
        this.logger = logger;
        instance = this;
        events = new Events();

        Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC::discordShutdown));
        initDiscord();

        ThreadFactoryBuilder builder = ThreadFactoryBuilder.builder().format("Tick Executor - #%d").build();
        tickExecutor = Executors.newScheduledThreadPool(10, builder);

        scheduler = new WaterdogScheduler();
        tickFuture = tickExecutor.scheduleAtFixedRate(this::processTick, 50, 50, TimeUnit.MILLISECONDS);

        if (!AppInfo.development && CacheManager.Settings.ENABLE_AUTO_UPDATE) scheduler.scheduleAsync(new UpdateCheckTask());
        App.getInstance().getScheduler().scheduleAsync(new FetchGatewayInformationTask());

        SwingUtilities.invokeLater(() -> ui = new AppUI());
        xboxUserInfo = CacheManager.loadXboxUserInfo();

        logger.info("App is in " + (AppInfo.alpha ? "Alpha" : (AppInfo.development ? "development" : "production")) + " mode");

        while (ui == null) {
            Thread.sleep(1000);
        }
        ui.setReady();
    }

    private void tick(int currentTick) {
        scheduler.scheduleAsync(DiscordRPC::discordRunCallbacks);
        if (socket != null) socket.tick(currentTick);
        if (App.ui != null) App.ui.tabs.forEach(t -> t.tick(currentTick));
        scheduler.onTick(currentTick);
    }

    private void shutdownServices() {
        tickExecutor.shutdown();
        scheduler.shutdown();
        if (socket != null) socket.shutdown();
    }

    public void updateServer(String new_network, String new_server) {
        String before_network = network_info.network;
        if (!Objects.equals(before_network, new_network)) {
            network_info.network = new_network;
            network_info.network_session_created = new_network == null ? null : Instant.now().toEpochMilli();
            network_info.server = null;
            network_info.server_session_created = network_info.network_session_created;
            events.onNetworkChange(before_network, new_network);
            return;
        }
        String before_server = network_info.server;
        if (!Objects.equals(before_server, new_server)) {
            network_info.server_session_created = new_server == null ? null : Instant.now().toEpochMilli();
            network_info.server = new_server;
            events.onNetworkServerChange(network_info.network, before_server, new_server);
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

        DiscordRPC.discordInitialize(application_id, handlers, true);
        DiscordRPC.discordRegister(application_id, null);
    }

    public static void setActivity(APIActivity api_activity) {
        setActivity(api_activity, true);
    }
    public static void setActivity(APIActivity api_activity, boolean queue) {
        App app = App.getInstance();
        if (api_activity == null) api_activity = APIActivity.none();
        if (api_activity.equals(app.discord_info.api_activity)) return;
        app.discord_info.api_activity = api_activity;
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

        if(ui != null) ui.general_tab.update(api_activity);
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

    public ImageIcon getTransparentBanner() {
        if (transparent_banner != null) return transparent_banner;
        try {
            //transparent_banner = new ImageIcon(ImageIO.read(new URL(Gateway.getUrl() + "/images/empty-banner.png")).getScaledInstance(500,1, 4));
            transparent_banner = new ImageIcon(ImageIO.read(new URL(Gateway.getUrl() + "/images/transparent-banner.png")).getScaledInstance(500,1, 4));
            return transparent_banner;
        } catch (IOException ignore) {
            return null;
        }
    }

    public final class Events implements IBaseListener {
        @Override
        public void onLogin(XboxUserInfo info) {
            xboxUserInfo = info;
            CacheManager.storeXboxUserInfo(info);
        }

        @Override
        public void onLogout() {
            xboxUserInfo = null;
            head_url = null;
            CacheManager.storeXboxUserInfo(null);
            setActivity(APIActivity.none());
        }

        @Override
        public void onDiscordReady(DiscordInfo info) {
            logger.debug("Welcome @" + info.getUsername() + ", discord-rpc is ready!");
            setActivity(APIActivity.none());
        }
    }
}

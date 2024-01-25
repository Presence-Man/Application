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

package xxAROX.PresenceMan.Application.ui.tabs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.entity.APIActivity;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.*;

public class FeaturedServersTab extends AUITab {
    private JComboBox<FeaturedServer> network;
    private JComboBox<__Server> game;
    private JComboBox<__Server> mode;


    public FeaturedServersTab(AppUI frame) {
        super(frame, "Featured server presence");
    }

    @Override
    protected void init(JPanel contentPane) {
        contentPane.setLayout(new GridBagLayout());

        {
            network = new JComboBox<>(Arrays.stream(FeaturedServers.values()).map(FeaturedServers::getServer).toList().toArray(new FeaturedServer[0]));
            network.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof FeaturedServer featuredServer) value = featuredServer.getName();
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            network.setBounds(0, 0, 500, 50);
            network.addActionListener(event -> refreshGame());
            contentPane.add(network);
        }
        {
            game = new JComboBox<>(new __Server[0]);
            game.addActionListener(event -> refreshMode());
            game.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof __Server server) value = server.getName();
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            contentPane.add(game);
        }

        {
            mode = new JComboBox<>(new __Server[0]);
            mode.setBounds(0, 0, 500, 50);
            mode.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof __Server server) value = server.getName();
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            contentPane.add(mode);
        }
        {
            JButton update_button = new JButton("Update");
            update_button.setBounds(0, 0, 500, 50);
            update_button.setBackground(new Color(53, 155, 47));
            update_button.setForeground(new Color(0, 0, 0));
            update_button.addActionListener(e -> {
                if (e.getActionCommand().equalsIgnoreCase(update_button.getText())) update_presence();
            });
            contentPane.add(update_button);
        }

        refreshGame();
        refreshMode();
    }
    protected void refreshGame(){
        FeaturedServer server = (FeaturedServer) network.getSelectedItem();
        game.removeAllItems();
        if (server != null && server.games != null) {
            for (Map.Entry<__Server, List<__Server>> entry : server.games.entrySet()) game.addItem(entry.getKey());
        }
        game.setVisible(game.getItemCount() > 0);
    }
    protected void refreshMode(){
        mode.removeAllItems();
        FeaturedServer server = (FeaturedServer) network.getSelectedItem();
        List<__Server> modes = server == null || server.getGames() == null ? new ArrayList<>() : server.getGames().get((__Server) this.game.getSelectedItem());
        if (modes == null) modes = new ArrayList<>();
        for (__Server mode : modes) this.mode.addItem(mode);
        mode.setVisible(mode.getItemCount() > 0);
    }

    protected void update_presence(){
        var network = (FeaturedServer) this.network.getSelectedItem();
        var game = (__Server) this.game.getSelectedItem();
        if (network == null || game == null) {
            App.getInstance().featuredServer = null;
            App.setActivity(APIActivity.none());
            return;
        }
        var mode = (__Server) this.mode.getSelectedItem();

        APIActivity activity = APIActivity.none();
        var before = App.getInstance().featuredServer;
        var featured_server = new xxAROX.PresenceMan.Application.entity.FeaturedServer(network.getName(), game, mode);
        if (before == null || !before.equals(featured_server)) {
            if (before == null || !before.getName().equalsIgnoreCase(featured_server.getName())) App.network_session_created = Instant.now().toEpochMilli();
            var before_server = before == null ? null : before.getGame().getName() + (before.getMode() != null ? " - " + before.getMode().getName() : "");
            var server = featured_server.getGame() + (featured_server.getMode() != null ? " - " + featured_server.getMode().getName() : "");
            if (before_server == null || !before_server.equalsIgnoreCase(server)) App.server_session_created = Instant.now().toEpochMilli();
            App.getInstance().featuredServer = featured_server;
        }
        activity
                .setDetails("Playing " + game.getName() + (mode == null ? "" : " - " + mode.getName()))
                .setState("on " + network.name)
        ;
        if (network.getIcon() != null) activity.setLarge_icon_key(network.getIcon());
        if (game.getIcon() != null) activity.setLarge_icon_key(game.getIcon());
        if (mode != null && mode.getIcon() != null) activity.setLarge_icon_key(mode.getIcon());
        App.setActivity(activity);
    }

    @AllArgsConstructor
    @ToString
    public enum FeaturedServers {
        NONE(new FeaturedServer("None", null, null)),
        HIVE(new FeaturedServer(
                "The Hive",
                new HashMap<>(){{
                put(new __Server("Hub", "hive-hub"), new ArrayList<>());
                put(new __Server("Treasure Wars", "hive-treasure-wars"), new ArrayList<>(){{
                    add(new __Server("Solos (No Teams)"));
                    add(new __Server("Duos (Teams of 2)"));
                    add(new __Server("Trios (Teams of 3)"));
                    add(new __Server("Squads (Teams of 4)"));
                    add(new __Server("MEGA (Teams of 12)"));
                }});
                put(new __Server("Just Build", "hive-just-build"), new ArrayList<>(){{
                    add(new __Server("Solos (No Teams)"));
                    add(new __Server("Solos (Double Build Time)"));
                    add(new __Server("Duos (Teams of 2)"));
                    add(new __Server("Duos (Double Build Time)"));
                }});
                put(new __Server("Capture the Flag", "hive-capture-the-flag"), new ArrayList<>(){{
                    add(new __Server("Regular"));
                }});
                put(new __Server("Hide and seek", "hive-hide-and-seek"), new ArrayList<>(){{
                    add(new __Server("Regular"));
                }});
                put(new __Server("Deathrun", "hive-deathrun"), new ArrayList<>(){{
                    add(new __Server("Regular"));
                }});
                put(new __Server("Murder", "hive-murder"), new ArrayList<>(){{
                    add(new __Server("Regular"));
                }});
                put(new __Server("Gravity", "hive-gravity"), new ArrayList<>(){{
                    add(new __Server("Regular"));
                }});
                put(new __Server("Block Party", "hive-block-party"), new ArrayList<>(){{
                    add(new __Server("Regular"));
                }});
                put(new __Server("Block Drop", "hive-block-drop"), new ArrayList<>(){{
                    add(new __Server("Regular"));
                }});
                put(new __Server("Ground Wars", "hive-ground-wars"), new ArrayList<>(){{
                    add(new __Server("Regular"));
                }});
                put(new __Server("Survival Games", "hive-survival-games"), new ArrayList<>(){{
                    add(new __Server("Solos (No Teams)"));
                    add(new __Server("Duos (Teams of 2)"));
                }});
                put(new __Server("The Bridge", "hive-the-bridge"), new ArrayList<>(){{
                    add(new __Server("Solos (Skill-Based)"));
                    add(new __Server("Duos (Teams of 2)"));
                }});
                put(new __Server("Skywars", "hive-skywars"), new ArrayList<>(){{
                    add(new __Server("Solos (No Teams)"));
                    add(new __Server("Duos (Teams of 2)"));
                    add(new __Server("Trios (Teams of 3)"));
                    add(new __Server("Trupps (Teams of 4)"));
                    add(new __Server("MEGA LTM (Teams of 7)"));
                }});
                put(new __Server("Skywars Kits", "hive-skywars-kits"), new ArrayList<>(){{
                    add(new __Server("Solos (No Teams)"));
                    add(new __Server("Duos (Teams of 2)"));
                }});
                put(new __Server("Replay-Cinema", "hive-replay-cinema"), new ArrayList<>());
            }},
                "hive"
        )),
        CUBECRAFT(new FeaturedServer(
                "CubeCraft",
                new HashMap<>(){{
                    put(new __Server("Lobby", "cubecraft-lobby"), new ArrayList<>());
                    put(new __Server("Beta games", "cubecraft-beta-games"), new ArrayList<>(){{
                        add(new __Server("Giga Blockwars"));
                    }});
                    put(new __Server("Eggwars", "cubecraft-eggwars"), new ArrayList<>(){{
                        add(new __Server("Solo"));
                        add(new __Server("Teams of 2"));
                        add(new __Server("Teams of 4"));
                        add(new __Server("Teams of 10"));
                        add(new __Server("Duels"));
                    }});
                    put(new __Server("Skywars", "cubecraft-skywars"), new ArrayList<>(){{
                        add(new __Server("Solo"));
                        add(new __Server("Teams of 2"));
                        add(new __Server("Teams of 4"));
                        add(new __Server("Teams of 10"));
                        add(new __Server("Duels"));
                        add(new __Server("Chaos"));
                    }});
                    put(new __Server("Lucky Blocks", "cubecraft-lucky-blocks"), new ArrayList<>(){{
                        add(new __Server("Solo"));
                        add(new __Server("Teams of 4"));
                        add(new __Server("Duels"));
                    }});
                    put(new __Server("Battle Arena", "cubecraft-battle-arena"), new ArrayList<>(){{
                        add(new __Server("Free for All"));
                        add(new __Server("Game Duels"));
                        add(new __Server("Duels"));
                    }});
                    put(new __Server("Blockwars", "cubecraft-blockwars"), new ArrayList<>(){{
                        add(new __Server("CTF Duels"));
                        add(new __Server("Capture the Flag"));
                        add(new __Server("Giga"));
                        add(new __Server("Bridges"));
                        add(new __Server("Bridges Duels"));
                    }});
                    put(new __Server("Skyblock", "cubecraft-skyblock"), new ArrayList<>());
                    put(new __Server("Mineware", "cubecraft-mineware"), new ArrayList<>());
                    put(new __Server("Parkour", "cubecraft-parkour"), new ArrayList<>());
                    put(new __Server("Survival Games", "cubecraft-survival-games"), new ArrayList<>(){{
                        add(new __Server("Solo"));
                        add(new __Server("Teams of 2"));
                    }});
                }},
                "cubecraft"
        ))
        ;

        private FeaturedServer server;

        public FeaturedServer getServer() {
            return server;
        }
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class FeaturedServer {
        private String name;
        private HashMap<__Server, List<__Server>> games;
        private String icon = null;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class __Server {
        private String name;
        private String icon = null;

        public __Server(String name) {
            this.name = name;
        }
    }
}

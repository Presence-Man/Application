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

import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.entity.APIActivity;
import xxAROX.PresenceMan.Application.entity.Gateway;
import xxAROX.PresenceMan.Application.entity.infos.XboxUserInfo;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.ui.popup.LoginPopup;
import xxAROX.PresenceMan.Application.utils.Lang;
import xxAROX.PresenceMan.Application.utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class GeneralTab extends AUITab {
    JLabel backend_status = new JLabel();
    JPanel server_info = new JPanel();

    public GeneralTab(AppUI frame) {
        super(frame, "ui.tab.home", "images/home.png");
    }

    @Override
    protected void init(JPanel contentPane) {
        contentPane.removeAll();
        boolean logged_in = App.getInstance().getXboxUserInfo() != null;

        contentPane.setLayout(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.ipadx = 20;
        constraints.ipady = 10;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;

        if(!logged_in) {
            JLabel pleaseLogIn = new JLabel(Lang.get("ui.tab.home.logged_out.title"), SwingConstants.CENTER);
            pleaseLogIn.setVisible(true);
            pleaseLogIn.setFocusable(false);
            pleaseLogIn.setFont(new Font("Arial", Font.BOLD, 20)); // NOTE: maybe change this to another

            constraints.gridx = 0;          // Column 0
            constraints.gridy = 0;          // Row 0 (top)
            constraints.gridwidth = 1;      // Span 1 column
            constraints.gridheight = 1;     // Span 1 row
            constraints.weightx = 1.0;      // Expand horizontally
            constraints.weighty = 0.0;      // Do not expand vertically
            constraints.anchor = GridBagConstraints.CENTER;   // Anchor to the top
            constraints.fill = GridBagConstraints.CENTER; // Fill horizontally



            contentPane.add(pleaseLogIn, constraints);

            var login_state_button = Utils.UIUtils.addButton(contentPane, constraints, 2, Lang.get("ui.tab.home.logged_out.button"), (button) -> login());
            login_state_button.setFocusPainted(false);
            login_state_button.setForeground(new Color(0, 0, 0));
            login_state_button.setFocusable(false);
            login_state_button.setBackground(new Color(0, 128, 0));

            //add info cursive text to bottom of the page
            JLabel info = new JLabel(Lang.get("ui.tab.home.logged_out.notice"), SwingConstants.CENTER);
            info.setVisible(true);
            info.setFocusable(false);
            info.setFont(new Font("Arial", Font.ITALIC, 12));

            constraints.gridx = 0;          // Column 0
            constraints.gridy = 3;          // Row 0 (top)
            constraints.gridwidth = 1;      // Span 1 column
            constraints.gridheight = 1;     // Span 1 row
            constraints.weightx = 1.0;      // Expand horizontally
            constraints.weighty = 0.0;      // Do not expand vertically
            constraints.fill = GridBagConstraints.CENTER; // Fill horizontally

            contentPane.add(info, constraints);

        } else {
            // SECTION: "logged in"
            var xboxInfo = App.getInstance().getXboxUserInfo();

            //show image left to name in 50x50px
            JLabel image = new JLabel();
            image.setIcon(new ImageIcon(xboxInfo.getProfileImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
            image.setVisible(true);
            image.setFocusable(false);

            constraints.anchor = GridBagConstraints.CENTER;   // Anchor to the top
            //display 70px left to the name
            constraints.gridx = 0;          // Column 0
            constraints.gridy = 0;          // Row 0 (top)
            constraints.gridwidth = 1;      // Span 1 column
            constraints.gridheight = 1;     // Span 1 row
            constraints.weightx = 0.0;      // Do not expand horizontally
            constraints.weighty = 0.0;      // Do not expand vertically
            constraints.fill = GridBagConstraints.CENTER; // Fill horizontally
            //add 70px offset to the left
            int offsetLeft = xboxInfo.getGamertag().length() * 10 + 40;
            constraints.insets = new Insets(0, 0, 0, offsetLeft);


            contentPane.add(image, constraints);


            // SECTION: "GAMERTAG"


            JLabel name = new JLabel(xboxInfo.getGamertag(), SwingConstants.CENTER);
            name.setVisible(true);
            name.setFocusable(false);
            name.setFont(new Font("Arial", Font.BOLD, 20));

            constraints.gridx = 0;          // Column 0
            constraints.gridy = 0;          // Row 0 (top)
            constraints.gridwidth = 1;      // Span 1 column
            constraints.gridheight = 1;     // Span 1 row
            constraints.weightx = 1.0;      // Expand horizontally
            constraints.weighty = 0.0;      // Do not expand vertically
            constraints.anchor = GridBagConstraints.CENTER;   // Anchor to the top
            constraints.fill = GridBagConstraints.CENTER; // Fill horizontally
            constraints.insets = new Insets(5, 5, 5, 5);

            contentPane.add(name, constraints);

            constraints.anchor = GridBagConstraints.WEST;

            //Discord Status (/Username) - 2nd row, italic
            JLabel discordStatus = new JLabel(Lang.get(App.getInstance().getDiscord_info().ready ? "ui.tab.home.discord.connected.yes" : "ui.tab.home.discord.connected.nop"), SwingConstants.CENTER);
            discordStatus.setVisible(true);
            discordStatus.setFocusable(false);
            discordStatus.setFont(new Font("Arial", Font.ITALIC, 12));

            constraints.gridx = 0;          // Column 0
            constraints.gridy = 1;          // Row 0 (top)
            constraints.gridwidth = 1;      // Span 1 column
            constraints.gridheight = 1;     // Span 1 row
            constraints.weightx = 1.0;      // Expand horizontally
            constraints.weighty = 0.0;      // Do not expand vertically
            constraints.fill = GridBagConstraints.CENTER; // Fill horizontally
            constraints.insets = new Insets(5, 5, 5, 100);

            contentPane.add(discordStatus, constraints);

            String username = App.getInstance().getDiscord_info().getUsername() != null ? "@" + App.getInstance().getDiscord_info().getUsername() : Lang.get("ui.tab.home.discord.connected.nop");

            JLabel dcUser = new JLabel(Lang.get("ui.tab.home.discord.user", new HashMap<>(){{put("{username}", username);}}), SwingConstants.CENTER);
            dcUser.setVisible(true);
            dcUser.setFocusable(false);
            dcUser.setFont(new Font("Arial", Font.ITALIC, 12));

            constraints.gridx = 0;          // Column 0
            constraints.gridy = 1;          // Row 0 (top)
            constraints.gridwidth = 1;      // Span 1 column
            constraints.gridheight = 1;     // Span 1 row
            constraints.weightx = 1.0;      // Expand horizontally
            constraints.weighty = 0.0;      // Do not expand vertically
            constraints.fill = GridBagConstraints.CENTER; // Fill horizontally
            constraints.insets = new Insets(5, 5, 30, 100);

            contentPane.add(dcUser, constraints);

            if(App.getInstance().getDiscord_info().getUsername() == null) {
                //add reconnect button below
                JButton reconnect = new JButton(Lang.get("ui.tab.home.discord.connected.rec"));
                reconnect.addActionListener(e -> {
                    App.getInstance().initDiscord();
                    init(contentPane);
                });
                constraints.gridx = 0;          // Column 0
                constraints.gridy = 1;          // Row 0 (top)
                constraints.gridwidth = 1;      // Span 1 column
                constraints.gridheight = 1;     // Span 1 row
                constraints.weightx = 1.0;      // Expand horizontally
                constraints.weighty = 0.0;      // Do not expand vertically
                constraints.fill = GridBagConstraints.CENTER; // Fill horizontally
                constraints.insets = new Insets(70, 5, 5, 25);

                contentPane.add(reconnect, constraints);
            }

            //Backend status

            constraints.anchor = GridBagConstraints.EAST;

            backend_status = new JLabel();
            backend_status.setText(Lang.get("ui.tab.home.backend_status.conn.pre"));
            backend_status.setHorizontalAlignment(SwingConstants.CENTER);
            backend_status.setVisible(true);
            backend_status.setFocusable(false);
            backend_status.setFont(new Font("Arial", Font.ITALIC, 12));

            constraints.gridx = 0;          // Column 0
            constraints.gridy = 1;          // Row 0 (top)
            constraints.gridwidth = 1;      // Span 1 column
            constraints.gridheight = 1;     // Span 1 row
            constraints.weightx = 1.0;      // Expand horizontally
            constraints.weighty = 0.0;      // Do not expand vertically
            constraints.fill = GridBagConstraints.CENTER; // Fill horizontally
            constraints.insets = new Insets(5, 100, 5, 5);

            contentPane.add(backend_status, constraints);

            //server_info
            GridBagConstraints serverConst = new GridBagConstraints();
            serverConst.fill = GridBagConstraints.HORIZONTAL;
            serverConst.insets = new Insets(5, 5, 5, 5);
            serverConst.ipadx = 20;
            serverConst.ipady = 10;
            serverConst.weightx = 0.5;
            serverConst.weighty = 0.5;


            server_info = new JPanel();
            server_info.setSize(500, 100);
            server_info.setLayout(new GridLayout(2, 1));
            server_info.setBorder(BorderFactory.createTitledBorder(Lang.get("ui.tab.home.server_info.title")));

            JLabel serverName = new JLabel(Lang.get("ui.tab.home.server_info.network.nop"), SwingConstants.CENTER);
            serverName.setForeground(Color.RED);
            serverName.setVisible(true);
            serverName.setFocusable(false);
            serverName.setFont(new Font("Arial", Font.ITALIC, 12));
            server_info.add(serverName, serverConst);

            //add details below

            serverConst.anchor = GridBagConstraints.CENTER;
            serverConst.gridx = 0;          // Column 0
            serverConst.gridy = 1;          // Row 0 (top)
            serverConst.gridwidth = 1;      // Span 1 column
            serverConst.gridheight = 1;     // Span 1 row
            serverConst.weightx = 1.0;      // Expand horizontally
            serverConst.weighty = 1.0;      // Do not expand vertically
            serverConst.fill = GridBagConstraints.CENTER; // Fill horizontally
            serverConst.insets = new Insets(5, 5, 5, 5);

            JLabel serverDetails = new JLabel("", SwingConstants.CENTER);
            serverDetails.setVisible(true);
            serverDetails.setFocusable(false);
            serverDetails.setFont(new Font("Arial", Font.ITALIC, 12));
            server_info.add(serverDetails, serverConst);

            serverConst.anchor = GridBagConstraints.CENTER;
            serverConst.gridx = 0;          // Column 0
            serverConst.gridy = 1;          // Row 0 (top)
            serverConst.gridwidth = 1;      // Span 1 column
            serverConst.gridheight = 1;     // Span 1 row
            serverConst.weightx = 1.0;      // Expand horizontally
            serverConst.weighty = 1.0;      // Do not expand vertically
            serverConst.fill = GridBagConstraints.CENTER; // Fill horizontally
            serverConst.insets = new Insets(5,5,5,5);

            JLabel serverBanner = new JLabel();
            //serverBanner.setSize(500,1);
            try {
                //serverBanner.setIcon(new ImageIcon(ImageIO.read(new URL(Gateway.getUrl() + "/images/empty-banner.png"))));
                serverBanner.setIcon(new ImageIcon(ImageIO.read(new URL(Gateway.getUrl() + "/images/transparent-banner.png")).getScaledInstance(500,1, 4)));
                serverBanner.setVisible(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            server_info.add(serverBanner, serverConst);


            //add server to contentPane in the bottom half
            constraints.gridx = 0;          // Column 0
            constraints.gridy = 2;          // Row 0 (top)
            constraints.gridwidth = 1;      // Span 1 column
            constraints.gridheight = 1;     // Span 1 row
            constraints.weightx = 1.0;      // Expand horizontally
            constraints.weighty = 1.0;      // Do not expand vertically
            constraints.fill = GridBagConstraints.CENTER; // Fill horizontally
            constraints.anchor = GridBagConstraints.CENTER;   // Anchor to the top
            constraints.insets = new Insets(5, 5, 5, 5);

            contentPane.add(server_info, constraints);



            // -- LOGIN / LOGOUT --
            {
                constraints.gridx = 1;
                constraints.gridy = 3;
                constraints.gridwidth = 1;
                constraints.anchor = GridBagConstraints.SOUTHEAST;
                constraints.fill = GridBagConstraints.NONE;
                constraints.weightx = 0;
                constraints.weighty = 1.0; // Push to the bottom
                var login_state_button = Utils.UIUtils.addButton(contentPane, constraints, 2, Lang.get("ui.tab.home.logged_in.button"), (button) -> logout());
                login_state_button.setFocusPainted(false);
                login_state_button.setForeground(new Color(0, 0, 0));
                login_state_button.setFocusable(false);
                login_state_button.setBackground(new Color(150, 56, 56));

            }
        }
    }

    public void update() {
        init(contentPane); // Way to fix #60
    }

    public void update(APIActivity activity) {
        if(server_info == null) return;
        boolean connected = App.getInstance().network_info.network != null && App.getInstance().network_info.server != null;
        boolean logged_in = App.getInstance().getXboxUserInfo() != null;
        if (!logged_in) return;

        var server_info_network = ((JLabel) server_info.getComponent(0));
        var server_info_server = ((JLabel) server_info.getComponent(1));
        if(!connected) {
            if (server_info_network != null) {
                server_info_network.setText(Lang.get("ui.tab.home.server_info.network.nop"));
                server_info_network.setForeground(Color.RED);
            }
            if (server_info_server != null) {
                server_info_server.setText(Lang.get("ui.tab.home.server_info.server.nop"));
            }
        } else {
            if (server_info_network != null) {
                server_info_network.setText(Lang.get("ui.tab.home.server_info.network.yes"));
                server_info_network.setForeground(new Color(0, 128, 0));
            }
            if (server_info_server != null) {
                server_info_server.setText(Lang.get("ui.tab.home.server_info.server.yes")); // TODO: more left
                // NOTE: next line on overflow
                server_info_server.setHorizontalAlignment(SwingConstants.LEFT);
            }
        }
    }

    @Override
    public void tick(int currentTick) {
        String status = switch (App.getInstance().getSocket().getConnectionState().get()) {
            case SHUTDOWN       -> "ui.tab.home.backend_status.conn.off";
            case DISCONNECTED   -> "ui.tab.home.backend_status.conn.nop";
            case CONNECTING     -> "ui.tab.home.backend_status.conn.may";
            case CONNECTED      -> "ui.tab.home.backend_status.conn.yes";
        };
        if (backend_status != null) backend_status.setText(Lang.get(status));

        if (currentTick %20 == 0) update(null); // TODO: maybe remove this
    }


    private LoginPopup login_popup;
    private Thread addThread;

    private void closeLoginPopup() {
        login_popup.markExternalClose();
        login_popup.setVisible(false);
        login_popup.dispose();
        login_popup = null;
    }
    private void login(){
        handleLogin(msaDeviceCodeConsumer -> {
            try {
                return new XboxUserInfo(XboxUserInfo.DEVICE_CODE_LOGIN.getFromInput(MinecraftAuth.createHttpClient(), new StepMsaDeviceCode.MsaDeviceCodeCallback(msaDeviceCodeConsumer)));
            } catch (Exception e) {
                if (e instanceof InterruptedException) return null;
                else if (e instanceof TimeoutException) {
                    SwingUtilities.invokeLater(() -> {
                        closeLoginPopup();
                        frame.showError("The login request timed out.\nPlease try again in 60 seconds.");
                    });
                } else {
                    App.getLogger().error(e);
                    App.ui.showException(e);
                }
            }
            return null;
        });
    }
    public void logout(){
        App.getEvents().onLogout();
        init(contentPane);
    }
    private void handleLogin(Function<Consumer<StepMsaDeviceCode.MsaDeviceCode>, XboxUserInfo> requestHandler) {
        this.addThread = new Thread(() -> {
            try {
                XboxUserInfo xboxUserInfo = requestHandler.apply(msaDeviceCode -> SwingUtilities.invokeLater(() -> new LoginPopup(frame, msaDeviceCode, popup -> login_popup = popup, () -> {
                    closeLoginPopup();
                    addThread.interrupt();
                })));
                if (xboxUserInfo == null) {
                    SwingUtilities.invokeLater(() -> {
                        closeLoginPopup();
                        this.frame.showError("Login failed, please try again later!");
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        closeLoginPopup();
                        App.getEvents().onLogin(xboxUserInfo);
                        init(contentPane);
                        frame.showInfo("Logged in as " + xboxUserInfo.getGamertag() + "!");
                    });
                }
            } catch (Throwable t) {
                if (t instanceof TimeoutException) {
                    SwingUtilities.invokeLater(() -> {
                        closeLoginPopup();
                        frame.showError("The login request timed out.\nPlease try again in 60 seconds.");
                    });
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    closeLoginPopup();
                    frame.showException(t);
                });
            }
        }, "Login thread");
        this.addThread.setDaemon(true);
        this.addThread.start();
    }
}

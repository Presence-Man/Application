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
import xxAROX.PresenceMan.Application.entity.XboxUserInfo;
import xxAROX.PresenceMan.Application.sockets.SocketThread;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.ui.popup.LoginPopup;
import xxAROX.PresenceMan.Application.utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class GeneralTab extends AUITab {
    private static final String NOT_CONNECTED = "Not connected";
    private static final String HALF_CONNECTED = "Connected to Presence-Man!";
    private static final String CONNECTED = "Connected to {server} on {network}";

    JLabel baStatus = new JLabel();

    JPanel server = new JPanel();

    public GeneralTab(AppUI frame) {
        super(frame, "Home");
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
            JLabel pleaseLogIn = new JLabel("Please log in with XBOX", SwingConstants.CENTER);
            pleaseLogIn.setVisible(true);
            pleaseLogIn.setFocusable(false);
            pleaseLogIn.setFont(new Font("Arial", Font.BOLD, 20));

            constraints.gridx = 0;          // Column 0
            constraints.gridy = 0;          // Row 0 (top)
            constraints.gridwidth = 1;      // Span 1 column
            constraints.gridheight = 1;     // Span 1 row
            constraints.weightx = 1.0;      // Expand horizontally
            constraints.weighty = 0.0;      // Do not expand vertically
            constraints.anchor = GridBagConstraints.CENTER;   // Anchor to the top
            constraints.fill = GridBagConstraints.CENTER; // Fill horizontally



            contentPane.add(pleaseLogIn, constraints);

            login_state_button = Utils.UIUtils.addButton(contentPane, constraints, 2, "Login with XBOX", (button) -> {
                login();
            });
            login_state_button.setFocusPainted(false);
            login_state_button.setForeground(new Color(0, 0, 0));
            login_state_button.setFocusable(false);
            login_state_button.setBackground(LOGIN_COLOR);

            //add info cursive text to bottom of the page
            JLabel info = new JLabel("Data will be stored locally. It is only used to verify you.", SwingConstants.CENTER);
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


            // ICON.GAMERTAG


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
            JLabel discordStatus = new JLabel("Discord Status: " + (App.getInstance().getDiscord_info().ready ? "Connected" : "Disconnected"), SwingConstants.CENTER);
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

            String username = App.getInstance().getDiscord_info().getUsername() != null ? "@" + App.getInstance().getDiscord_info().getUsername() : "Not connected";

            JLabel dcUser = new JLabel("Discord User: " + username, SwingConstants.CENTER);
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
                JButton reconnect = new JButton("Reconnect");
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

            String status = "Loading...";

            baStatus = new JLabel();
            baStatus.setText("Status: " + status);
            baStatus.setHorizontalAlignment(SwingConstants.CENTER);
            baStatus.setVisible(true);
            baStatus.setFocusable(false);
            baStatus.setFont(new Font("Arial", Font.ITALIC, 12));

            constraints.gridx = 0;          // Column 0
            constraints.gridy = 1;          // Row 0 (top)
            constraints.gridwidth = 1;      // Span 1 column
            constraints.gridheight = 1;     // Span 1 row
            constraints.weightx = 1.0;      // Expand horizontally
            constraints.weighty = 0.0;      // Do not expand vertically
            constraints.fill = GridBagConstraints.CENTER; // Fill horizontally
            constraints.insets = new Insets(5, 100, 5, 5);

            contentPane.add(baStatus, constraints);

            //server
            GridBagConstraints serverConst = new GridBagConstraints();
            serverConst.fill = GridBagConstraints.HORIZONTAL;
            serverConst.insets = new Insets(5, 5, 5, 5);
            serverConst.ipadx = 20;
            serverConst.ipady = 10;
            serverConst.weightx = 0.5;
            serverConst.weighty = 0.5;



            server = new JPanel();
            server.setSize(500, 100);
            server.setLayout(new GridLayout(2, 1));
            server.setBorder(BorderFactory.createTitledBorder("Server Info"));

            JLabel serverName = new JLabel("Not connected", SwingConstants.CENTER);
            serverName.setForeground(Color.RED);
            serverName.setVisible(true);
            serverName.setFocusable(false);
            serverName.setFont(new Font("Arial", Font.ITALIC, 12));
            server.add(serverName, serverConst);

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
            server.add(serverDetails, serverConst);

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
            serverBanner.setSize(500,1);
            try {
                serverBanner.setIcon(new ImageIcon(ImageIO.read(new URL(Gateway.getUrl() + "/home/images/empty-banner.png")).getScaledInstance(500,1, 4)));
                serverBanner.setVisible(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            server.add(serverBanner, serverConst);


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

            contentPane.add(server, constraints);



            // -- LOGIN / LOGOUT --
            {
                constraints.gridx = 1;
                constraints.gridy = 3;
                constraints.gridwidth = 1;
                constraints.anchor = GridBagConstraints.SOUTHEAST;
                constraints.fill = GridBagConstraints.NONE;
                constraints.weightx = 0;
                constraints.weighty = 1.0; // Push to the bottom
                login_state_button = Utils.UIUtils.addButton(contentPane, constraints, 2, LOGOUT_TEXT, (button) -> {
                    logout();
                });
                login_state_button.setFocusPainted(false);
                login_state_button.setForeground(new Color(0, 0, 0));
                login_state_button.setFocusable(false);
                login_state_button.setBackground(LOGOUT_COLOR);

            }
        }
    }

    public void update() {
        init(contentPane);
    }

    public void update(APIActivity activity) {
        if(server == null) return;
        boolean connected = App.getInstance().getNetwork() != null && App.getInstance().getServer() != null;

        if(!connected) {
            ((JLabel) server.getComponent(0)).setText("Not connected to a server");
            ((JLabel) server.getComponent(0)).setForeground(Color.RED);

            ((JLabel) server.getComponent(1)).setText("");
        } else {
            ((JLabel) server.getComponent(0)).setText("Connected to " + App.getInstance().getNetwork());
            ((JLabel) server.getComponent(0)).setForeground(Color.GREEN);

            ((JLabel) server.getComponent(1)).setText("Server: " + App.getInstance().getServer());
            //next line on overflow
            ((JLabel) server.getComponent(1)).setHorizontalAlignment(SwingConstants.LEFT);

        }

    }

    @Override
    public void tick(int currentTick) {
        String status;

        try {
            status = (App.getInstance().getSocket().getConnectionState().get().toString());
            //only first letter uppercase
            status = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        } catch (Exception e) {
            status = "Not connected";
        }

        if(baStatus != null) baStatus.setText("Status: " + status);
    }

    private static String getConnectedMessage(){
        var activity = App.getInstance().getApi_activity();
        boolean connected = activity != null && App.getInstance().getNetwork() != null && App.getInstance().getServer() != null;
        var connected_to_backend = SocketThread.getInstance() != null && SocketThread.getInstance().getConnectionState().get().equals(SocketThread.State.CONNECTED);
        return connected ?
                CONNECTED
                    .replace("{server}", App.getInstance().getServer())
                    .replace("{network}", App.getInstance().getNetwork())
                //: (Gateway.connected ? HALF_CONNECTED : NOT_CONNECTED);
                : (connected_to_backend ? HALF_CONNECTED : NOT_CONNECTED);
    }


    private static final String LOGIN_TEXT = "Login";
    public static final Color LOGIN_COLOR = new Color(59, 155, 57);
    private static final String LOGOUT_TEXT = "Logout";
    public static final Color LOGOUT_COLOR = new Color(150, 56, 56);
    private JButton login_state_button;
    private LoginPopup login_popup;
    private Thread addThread;
    private void reloadStateButton(){
        boolean logged_in = App.getInstance().getXboxUserInfo() != null;
        login_state_button.setText(logged_in ? LOGOUT_TEXT : LOGIN_TEXT);
        login_state_button.setBackground(logged_in ? LOGOUT_COLOR : LOGIN_COLOR);
    }
    private void closeLoginPopup() {
        login_popup.markExternalClose();
        login_popup.setVisible(false);
        login_popup.dispose();
        login_popup = null;
        login_state_button.setEnabled(true);
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
        reloadStateButton();
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
                        reloadStateButton();
                        frame.showInfo("Logged in as " + xboxUserInfo.getGamertag() + "!");
                        init(contentPane);
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

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
import xxAROX.PresenceMan.Application.entity.XboxUserInfo;
import xxAROX.PresenceMan.Application.sockets.SocketThread;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.ui.popup.LoginPopup;
import xxAROX.PresenceMan.Application.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class GeneralTab extends AUITab {
    private static final String NOT_CONNECTED = "Not connected";
    private static final String HALF_CONNECTED = "Connected to Presence-Man!";
    private static final String CONNECTED = "Connected to {server} on {network}";

    JLabel baStatus = new JLabel();

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

            JLabel dcUser = new JLabel("Discord User: @" + App.getInstance().getDiscord_info().getUsername(), SwingConstants.CENTER);
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




            // -- LOGIN / LOGOUT --
            {
                constraints.gridx = 1;
                constraints.gridy = 3;
                constraints.gridwidth = 1;
                constraints.anchor = GridBagConstraints.SOUTHEAST;
                constraints.fill = GridBagConstraints.NONE;
                constraints.weightx = 0;
                constraints.weighty = 1.0; // Push to the bottom
                login_state_button = Utils.UIUtils.addButton(contentPane, constraints, 2, logged_in ? LOGOUT_TEXT : LOGIN_TEXT, (button) -> {
                    if (App.getInstance().getXboxUserInfo() != null) logout();
                    else login();
                });
                login_state_button.setFocusPainted(false);
                login_state_button.setForeground(new Color(0, 0, 0));
                login_state_button.setFocusable(false);
                login_state_button.setBackground(logged_in ? LOGOUT_COLOR : LOGIN_COLOR);

            }
        }
    }

    @Override
    public void setReady() {
    }

    @Override
    public void onClose() {
    }

    public void update() {
        init(contentPane);
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

    private String getLoggedInMessage() {
        var xboxInfo = App.getInstance().getXboxUserInfo();
        boolean logged_in = xboxInfo != null;
        return logged_in ? "Logged in as " + xboxInfo.getGamertag() : "Not logged into Xbox account.";
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

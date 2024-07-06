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
    private static final String HALF_CONNECTED = "Connected to Presence-Man backend!";
    private static final String CONNECTED = "Connected to {server} on {network}";

    JLabel label_loggedin_status;
    JLabel label_connection_status;

    public GeneralTab(AppUI frame) {
        super(frame, "Home");
    }

    @Override
    protected void init(JPanel contentPane) {
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.ipadx = 20;
        constraints.ipady = 10;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;

        label_loggedin_status = new JLabel(getLoggedInMessage(), SwingConstants.CENTER);
        label_loggedin_status.setVisible(true);
        label_loggedin_status.setFocusable(false);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weighty = 0;
        contentPane.add(label_loggedin_status, constraints);

        label_connection_status = new JLabel(getConnectedMessage(), SwingConstants.CENTER);
        label_connection_status.setVisible(true);
        label_connection_status.setFocusable(false);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 0;
        contentPane.add(label_connection_status, constraints);

        // -- LOGIN / LOGOUT --
        {
            boolean logged_in = App.getInstance().getXboxUserInfo() != null;
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

    @Override
    public void setReady() {
    }

    @Override
    public void onClose() {
    }

    public void tick() {
        {
            var text = getLoggedInMessage();
            if (!label_loggedin_status.getText().equals(text)) label_loggedin_status.setText(text);
        }
        {
            var text = getConnectedMessage();
            if (!label_connection_status.getText().equals(text)) label_connection_status.setText(text);
        }
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
                        frame.showError("The login request timed out.\nPlease login within 60 seconds.");
                    });
                } else {
                    App.getLogger().error(e);
                    App.ui.showException(e);
                }
            }
            return null;
        });
    }
    private void logout(){
        App.getEvents().onLogout();
        reloadStateButton();
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
                        this.frame.showError("Login failed, please try again!");
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        closeLoginPopup();
                        App.getEvents().onLogin(xboxUserInfo);
                        reloadStateButton();
                        frame.showInfo("Logged in as " + xboxUserInfo.getGamertag() + "!");
                    });
                }
            } catch (Throwable t) {
                if (t instanceof TimeoutException) {
                    SwingUtilities.invokeLater(() -> {
                        closeLoginPopup();
                        frame.showError("The login request timed out.\nPlease login within 60 seconds.");
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

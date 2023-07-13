package xxAROX.PresenceMan.Application.ui.tabs;

import net.raphimc.mcauth.MinecraftAuth;
import net.raphimc.mcauth.step.msa.StepMsaDeviceCode;
import net.raphimc.mcauth.util.MicrosoftConstants;
import org.apache.http.impl.client.CloseableHttpClient;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.entity.XboxUserInfo;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.ui.popup.LoginPopup;
import xxAROX.PresenceMan.Application.utils.CacheManager;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class LoginTab extends AUITab {
    private JButton stateButton;
    private LoginPopup login_popup;
    private Thread addThread;

    public LoginTab(AppUI frame) {
        super(frame, "Link XBOX account");
    }

    private void reloadStateButton(){
        XboxUserInfo info = App.getInstance().getXboxUserInfo();
        if (stateButton != null) {
            stateButton.setToolTipText(info == null ? null : "Logged in as " + info.getGamertag());
            stateButton.setText(info == null ? "Login" : "Logout");
            stateButton.setBackground(info == null ? new Color(59, 155, 57) : new Color(150, 56, 56));
        }
    }

    @Override
    protected void init(JPanel contentPane) {
        XboxUserInfo info = App.getInstance().getXboxUserInfo();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        {
            stateButton = new JButton(info == null ? "Login" : "Logout");
            stateButton.setFocusPainted(false);
            stateButton.setForeground(new Color(0, 0, 0));
            stateButton.setFocusable(false);
            stateButton.setBackground(info == null ? new Color(59, 155, 57) : new Color(150, 56, 56));
            stateButton.addActionListener(event -> {
                if (stateButton.getText().equalsIgnoreCase("Login")) login();
                else if (stateButton.getText().equalsIgnoreCase("Logout")) logout();
            });
            stateButton.setVisible(false);
            stateButton.setPreferredSize(new Dimension(315, 45));
            stateButton.setHorizontalAlignment(SwingConstants.CENTER);
            stateButton.setVerticalAlignment(SwingConstants.CENTER);


            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.fill = GridBagConstraints.CENTER;

            contentPane.add(stateButton, constraints);
        }
    }

    @Override
    public void setReady() {
        stateButton.setVisible(true);
    }



    private void closePopup() {
        this.login_popup.markExternalClose();
        this.login_popup.setVisible(false);
        this.login_popup.dispose();
        this.login_popup = null;
        this.stateButton.setEnabled(true);
    }

    private void login(){
        handleLogin(msaDeviceCodeConsumer -> {
            try (CloseableHttpClient httpClient = MicrosoftConstants.createHttpClient()) {
                return new XboxUserInfo(MinecraftAuth.BEDROCK_DEVICE_CODE_LOGIN.getFromInput(httpClient, new StepMsaDeviceCode.MsaDeviceCodeCallback(msaDeviceCodeConsumer)));
            } catch (Exception e) {
                if (e instanceof InterruptedException) return null;
                else if (e instanceof TimeoutException) {
                    SwingUtilities.invokeLater(() -> {
                        closePopup();
                        frame.showError("The login request timed out.\nPlease login within 60 seconds.");
                    });
                } else {
                    App.getInstance().getLogger().error(e);
                    App.ui.showException(e);
                }
            }
            return null;
        });
    }

    private void logout(){
        App.getInstance().onLogout();
        reloadStateButton();
    }

    private void handleLogin(Function<Consumer<StepMsaDeviceCode.MsaDeviceCode>, XboxUserInfo> requestHandler) {
        this.addThread = new Thread(() -> {
            try {
                XboxUserInfo xboxUserInfo = requestHandler.apply(msaDeviceCode -> {
                    SwingUtilities.invokeLater(() -> {
                        new LoginPopup(frame, msaDeviceCode, popup -> login_popup = popup, () -> {
                            closePopup();
                            addThread.interrupt();
                        });
                    });
                });
                if (xboxUserInfo == null) {
                    frame.showError("Login failed, please try again!");
                } else {
                    SwingUtilities.invokeLater(() -> {
                        closePopup();
                        App.getInstance().xboxUserInfo = xboxUserInfo;
                        CacheManager.storeXboxUserInfo(xboxUserInfo);
                        App.getInstance().onLogin();
                        reloadStateButton();
                        frame.showInfo("Logged in as " + xboxUserInfo.getGamertag() + "!");
                    });
                }
            } catch (Throwable t) {
                if (t instanceof TimeoutException) {
                    SwingUtilities.invokeLater(() -> {
                        this.closePopup();
                        this.frame.showError("The login request timed out.\nPlease login within 60 seconds.");
                    });
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    this.closePopup();
                    this.frame.showException(t);
                });
            }
        }, "Login thread");
        this.addThread.setDaemon(true);
        this.addThread.start();
    }

}

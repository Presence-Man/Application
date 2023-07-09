package xxAROX.PresenceMan.Application.ui.tabs;

import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;

import javax.swing.*;
import java.awt.*;

public class GeneralTab extends AUITab {
    private JButton stateButton;

    public GeneralTab(AppUI frame) {
        super(frame, "General");
    }

    @Override
    protected void init(JPanel contentPane) {
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        {
            stateButton = new JButton("Connect");
            stateButton.setBackground(new Color(98, 80, 217));
            stateButton.addActionListener(event -> {
                if (stateButton.getText().equalsIgnoreCase("Connect")) start();
                else if (stateButton.getText().equalsIgnoreCase("Disconnect")) stop();
            });
            stateButton.setVisible(false);
            stateButton.setPreferredSize(new Dimension(315, 45));

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.fill = GridBagConstraints.CENTER;

            contentPane.add(stateButton, constraints);
        }
    }

    @Override
    public void setReady() {
        SwingUtilities.invokeLater(() -> {
            stateButton.setText("Connect");
            stateButton.setVisible(true);
            stateButton.setEnabled(true);
        });
    }

    @Override
    public void onClose() {
    }

    private void setComponentsEnabled(boolean state) {
        stateButton.setEnabled(state);
    }

    private void start() {
        this.setComponentsEnabled(false);
        this.stateButton.setEnabled(false);
        this.stateButton.setText("Connecting..");

        new Thread(() -> {
            try {
                try {
                    /*

                    SwingUtilities.invokeLater(() -> {
                        this.frame.showError(t.getMessage());
                    });

                     */
                    //Connection.connect();
                } catch (Throwable e) {
                    SwingUtilities.invokeLater(() -> {
                        this.frame.showError("Failed to connect to " + AppInfo.name + " backend! Ensure that you are online and try again.");
                    });
                    throw e;
                }

                SwingUtilities.invokeLater(() -> {
                    this.stateButton.setEnabled(true);
                    this.stateButton.setText("Disconnect");
                });
            } catch (Throwable e) {
                App.getInstance().getLogger().error("Error while starting " + AppInfo.name, e);
                SwingUtilities.invokeLater(() -> {
                    this.setComponentsEnabled(true);
                    this.stateButton.setEnabled(true);
                    this.stateButton.setText("Connect");
                });
            }
        }).start();
    }

    private void stop() {
        //Connection.disconnect();
        this.stateButton.setText("Connect");
        this.setComponentsEnabled(true);
    }
}

package xxAROX.PresenceMan.Application.ui.tabs;

import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;

import javax.swing.*;
import java.awt.*;

public class GeneralTab extends AUITab {
    private static final String NOT_CONNECTED = "Not connected";
    JLabel label_connected;

    public GeneralTab(AppUI frame) {
        super(frame, "Home");
    }

    @Override
    protected void init(JPanel contentPane) {
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        var a = App.getInstance().getApi_activity();
        boolean connected = a != null && App.getInstance().getConnection() != null;
        label_connected = new JLabel(connected ? "Connected to " + App.getInstance().getConnection().getNetwork() : NOT_CONNECTED);
        label_connected.setVisible(true);
        label_connected.setBounds(10, 10, 17, 17);
        contentPane.add(label_connected);
    }

    public void tick() {
        var a = App.getInstance().getApi_activity();
        boolean connected = a != null && App.getInstance().getConnection() != null;
        var text = connected ? "Connected to " + App.getInstance().getConnection().getNetwork() : NOT_CONNECTED;
        if (!label_connected.getText().equals(text)) label_connected.setText(text);
    }

    @Override
    public void setReady() {
    }

    @Override
    public void onClose() {
    }
}

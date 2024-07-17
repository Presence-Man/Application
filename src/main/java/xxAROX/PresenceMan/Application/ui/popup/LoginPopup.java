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

package xxAROX.PresenceMan.Application.ui.popup;

import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.utils.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class LoginPopup extends JDialog {
    private final AppUI parent;
    private final StepMsaDeviceCode.MsaDeviceCode deviceCode;
    private boolean externalClose;

    public LoginPopup(AppUI parent, StepMsaDeviceCode.MsaDeviceCode deviceCode, Consumer<LoginPopup> popupConsumer, Runnable closeListener) {
        super(parent, true);
        this.parent = parent;
        this.deviceCode = deviceCode;
        popupConsumer.accept(this);

        this.initWindow(closeListener);
        this.initComponents();
        this.setVisible(true);
    }

    private void initWindow(final Runnable closeListener) {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!externalClose) closeListener.run();
            }
        });
        this.setTitle(Lang.get("ui.popup.login.title"));
        this.setSize(400, 200);
        this.setResizable(false);
        this.setLocationRelativeTo(this.parent);
    }

    private void initComponents() {
        App.ui.openURL(deviceCode.getDirectVerificationUri());

        JPanel contentPane = new JPanel();
        contentPane.setLayout(null);

        JLabel browserLabel = new JLabel(Lang.get("ui.popup.login.browser"));
        browserLabel.setBounds(10, 45, 380, 20);
        contentPane.add(browserLabel);

        JLabel closeInfo = new JLabel(Lang.get("ui.popup.login.closing"));
        closeInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        closeInfo.setBounds(10, 75, 380, 20);
        contentPane.add(closeInfo);
        setContentPane(contentPane);
    }

    public void markExternalClose() {
        this.externalClose = true;
    }
}

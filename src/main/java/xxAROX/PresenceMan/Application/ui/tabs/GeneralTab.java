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

import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.sockets.SocketThread;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;

import javax.swing.*;
import java.awt.*;

public class GeneralTab extends AUITab {
    private static final String NOT_CONNECTED = "Not connected";
    private static final String HALF_CONNECTED = "Connected to Presence-Man backend!";
    private static final String CONNECTED = "Connected to {server} on {network}";

    JLabel label_connection_status;

    public GeneralTab(AppUI frame) {
        super(frame, "Home");
    }

    @Override
    protected void init(JPanel contentPane) {
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        label_connection_status = new JLabel(getConnectedMessage(), SwingConstants.CENTER);
        label_connection_status.setVisible(true);
        contentPane.add(label_connection_status);
    }

    @Override
    public void setReady() {
    }

    @Override
    public void onClose() {
    }

    public void tick() {
        updateConnectedMessage();
    }

    public void updateConnectedMessage(){
        var text = getConnectedMessage();
        if (!label_connection_status.getText().equals(text)) label_connection_status.setText(text);
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
}

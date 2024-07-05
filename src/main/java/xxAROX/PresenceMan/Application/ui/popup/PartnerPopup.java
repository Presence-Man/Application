/*
 * Copyright (c) $originalComment.match("Copyright \(c\) (\d+)", 1, "-", "$today.year")2024. By Jan-Michael Sohn also known as @xxAROX.
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

import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.ui.tabs.PartnersTab;

import javax.swing.*;
import java.awt.*;

public class PartnerPopup extends JDialog {
    private final AppUI parent;
    private final PartnersTab.PartnerItem partner;

    public PartnerPopup(AppUI parent, PartnersTab.PartnerItem partner) {
        super(parent, true);
        this.parent = parent;
        this.partner = partner;
        this.initWindow();
        this.initComponents();
        this.setVisible(true);
    }

    private void initWindow() {
        this.setTitle(partner.title);
        this.setSize(400, 400);
        this.setResizable(false);
        this.setLocationRelativeTo(this.parent);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new GridLayout());

        JLabel browserLabel = new JLabel("TODO");
        browserLabel.setBounds(10, 10, 380, 20);
        contentPane.add(browserLabel, BorderLayout.CENTER);

        //if (domain != null) App.ui.openURL("minecraft://?addExternalServer=" + (partnerItem.display_name) + "§r|" + domain + ":19132");


        setContentPane(contentPane);
    }
}

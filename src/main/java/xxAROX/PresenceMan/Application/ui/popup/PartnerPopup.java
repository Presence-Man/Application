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

import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.ui.tabs.PartnersTab;

import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        contentPane.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);

        if (partner.banner_icon != null) {
            JLabel banner = new JLabel(new ImageIcon(partner.banner_icon.getImage().getScaledInstance(340, 125, Image.SCALE_SMOOTH)));
            topPanel.add(banner, BorderLayout.CENTER);
            topPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        } else if (partner.icon != null) {
            JLabel icon = new JLabel(new ImageIcon(partner.icon.getImage().getScaledInstance(200, 300, Image.SCALE_SMOOTH)));
            topPanel.add(icon, BorderLayout.CENTER);
            topPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        }
        if (partner.banner_icon != null || partner.icon != null) contentPane.add(topPanel, BorderLayout.NORTH);

        int description_side_space = 10;
        JTextArea description = new JTextArea("\n\n\n" + partner.about_text);
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setEditable(false);
        description.setOpaque(false);
        description.setBorder(BorderFactory.createEmptyBorder(description_side_space, description_side_space, description_side_space, description_side_space));

        JScrollPane scrollPane = new JScrollPane(description);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, partner.url != null ? 2 : 1, 10, 0));

        JButton add_server_btn = new JButton("Add server");
        add_server_btn.setPreferredSize(new Dimension(150, 50));
        add_server_btn.addActionListener(e -> {
            add_server_btn.setFocusPainted(false);
            if (partner.domain != null) App.ui.openURL("minecraft://?addExternalServer=" + URLEncoder.encode(partner.title + "§r|", StandardCharsets.UTF_8) + partner.domain + ":19132");
        });
        buttonPanel.add(add_server_btn);

        if (partner.url != null) {
            boolean is_discord_url = partner.url.toLowerCase().contains("discord") || partner.url.toLowerCase().contains("disc") || partner.url.toLowerCase().contains("dc");
            JButton url_btn = new JButton(is_discord_url ? "Discord" : "Website");
            url_btn.setPreferredSize(new Dimension(150, 50));
            if (is_discord_url) url_btn.setBackground(new Color(88, 101, 242));
            url_btn.addActionListener(e -> {
                url_btn.setFocusPainted(false);
                if (partner.url != null && partner.url.startsWith("http")) App.ui.openURL(partner.url);
            });
            buttonPanel.add(url_btn);
        }

        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(contentPane);
    }
}

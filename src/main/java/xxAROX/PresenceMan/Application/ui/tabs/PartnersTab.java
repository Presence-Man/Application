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

package xxAROX.PresenceMan.Application.ui.tabs;

import com.google.gson.JsonObject;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;
import xxAROX.PresenceMan.Application.Bootstrap;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.ui.popup.PartnerPopup;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PartnersTab extends AUITab {
    private List<PartnerItem> partnerItems;

    public PartnersTab(AppUI frame) {
        super(frame, "Partners");
    }
    @Override
    protected void init(JPanel contentPane) {
        partnerItems = new ArrayList<>();

        contentPane.setLayout(new BorderLayout());
        var border_size = 0;
        contentPane.setBorder(BorderFactory.createEmptyBorder(border_size, border_size, border_size, border_size));

        JPanel base = new JPanel();
        base.setLayout(new GridLayout(0, 3, 10, 10)); // 3 items per row, with 10px horizontal and vertical gap
        JScrollPane scrollPane = new JScrollPane(base);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(13);

        var icon = Objects.requireNonNull(Bootstrap.class.getClassLoader().getResource(AppInfo.icon));
        partnerItems.add(new PartnerItem("Partner 1", "§1{title}", null, null, new ImageIcon(icon)));
        partnerItems.add(new PartnerItem("Partner 2", "§1{title}", null, null, new ImageIcon(icon)));
        partnerItems.add(new PartnerItem("Partner 3", "§1{title}", null, null, new ImageIcon(icon)));

        partnerItems.add(new PartnerItem("Partner 4", "§1{title}", null, null ,new ImageIcon(icon)));
        partnerItems.add(new PartnerItem("Partner 5", "§1{title}", null, null, new ImageIcon(icon)));
        partnerItems.add(new PartnerItem("Partner 6", "§1{title}", null, null, new ImageIcon(icon)));

        partnerItems.add(new PartnerItem("Partner 7", "§1{title}", null, null ,new ImageIcon(icon)));
        partnerItems.add(new PartnerItem("Partner 8", "§1{title}", null, null, new ImageIcon(icon)));
        partnerItems.add(new PartnerItem("Partner 9", "§1{title}", null, null, new ImageIcon(icon)));


        // Set preferred size of scroll pane to control initial size
        scrollPane.setPreferredSize(new Dimension(400, 300));

        for (PartnerItem item : partnerItems) base.add(item);

        // Add the scroll pane to the main panel (this class extends JPanel presumably)
        contentPane.add(scrollPane, BorderLayout.CENTER);
    }

    public static class PartnerItem extends JPanel {
        public String title;
        public String display_name;
        public String about_text;
        public String domain;
        public ImageIcon icon;
        public ImageIcon banner_icon;
        public boolean enabled;
        public String url;

        public PartnerItem(JsonObject json) throws MalformedURLException {
            String title = json.get("title").getAsString();
            String display_name = json.has("display_name") && !json.get("display_name").isJsonNull() ? json.get("display_name").getAsString() : title;
            String domain = json.has("domain") && !json.get("domain").isJsonNull() ? json.get("domain").getAsString() : null;
            String image = json.has("image") && !json.get("image").isJsonNull() ? json.get("image").getAsString() : null;
            var icon = image == null ? null : (image.startsWith("http") ? new ImageIcon(new URL(image)) : new ImageIcon(image));
            String banner_image = json.has("banner") && !json.get("banner").isJsonNull() ? json.get("banner").getAsString() : null;
            var banner_icon = banner_image == null ? null : (banner_image.startsWith("http") ? new ImageIcon(new URL(banner_image)) : new ImageIcon(banner_image));
            boolean enabled = json.has("enabled") && !json.get("enabled").isJsonNull() && json.get("enabled").getAsBoolean();
            String about_text = json.has("about_text") ? json.get("about_text").getAsString() : "No description provided!";
            String url = json.has("url") ? json.get("url").getAsString() : null;
            build(title, display_name, about_text, domain, icon, banner_icon, enabled, url);
        }
        public PartnerItem(String title) {build(title, title, null, null, null, null, true, null);}
        public PartnerItem(String title, String display_name) {build(title, display_name, null, null, null, null, true, null);}
        public PartnerItem(String title, String display_name, String about_text) {build(title, display_name, about_text, null, null, null, true, null);}
        public PartnerItem(String title, String display_name, String about_text, String domain) {build(title, display_name, about_text, domain, null, null, true, null);}
        public PartnerItem(String title, String display_name, String about_text, String domain, ImageIcon icon) {build(title, display_name, about_text, domain, icon, null, true, null);}
        public PartnerItem(String title, String display_name, String about_text, String domain, ImageIcon icon, ImageIcon banner_icon, boolean enabled) {build(title, display_name, about_text, domain, icon, banner_icon, enabled, null);}
        public PartnerItem(String title, String display_name, String about_text, String domain, ImageIcon icon, ImageIcon banner_icon, boolean enabled, String url) {build(title, display_name, about_text, domain, icon, banner_icon, enabled, url);}

        private void build(String title, String display_name, String about_text, String domain, ImageIcon icon, ImageIcon banner_icon, boolean enabled, String url) {
            this.title = title;
            this.display_name = display_name == null ? title : display_name.replace("{title}", title);
            this.about_text = about_text == null ? "No description provided!" : about_text;
            this.domain = domain;
            this.icon = icon;
            this.banner_icon = banner_icon;
            this.enabled = enabled;
            this.url = url;

            if (icon != null) icon = new ImageIcon(icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));

            setLayout(new GridLayout());

            // Button
            JButton button = new JButton(title);
            button.setIcon(icon);
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
            button.setEnabled(enabled);
            button.setOpaque(true);

            button.addActionListener(e -> {
                button.setFocusPainted(false);
                new PartnerPopup(App.ui, this);
            });
            add(button, BorderLayout.CENTER);
        }
    }
}

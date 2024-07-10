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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.ui.popup.PartnerPopup;
import xxAROX.PresenceMan.Application.utils.Lang;
import xxAROX.PresenceMan.Application.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PartnersTab extends AUITab {
    private List<PartnerItem> partnerItems = new ArrayList<>();
    boolean loaded = false;
    JPanel base = new JPanel();
    JScrollPane scrollPane = new JScrollPane(base);

    public PartnersTab(AppUI frame) {
        super(frame, "Partners", "images/partners.png");
    }
    @Override
    protected void init(JPanel contentPane) {
        contentPane.removeAll();

        if(!loaded) {
            base = new JPanel();
            scrollPane = new JScrollPane(base);

            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getVerticalScrollBar().setUnitIncrement(13);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JLabel loading = new JLabel(Lang.get("ui.tab.partners.loading"), SwingConstants.CENTER);
            loading.setFont(new Font(loading.getFont().getName(), Font.BOLD, 20));
            contentPane.add(loading, BorderLayout.CENTER);
            contentPane.repaint();

            new Thread(() -> {
                reloadPartners();
                loaded = true;

                int itemCount = partnerItems.size();
                if (itemCount == 1) base.setLayout(new GridBagLayout());
                else if (itemCount == 2) base.setLayout(new GridBagLayout());
                else base.setLayout(new GridLayout(0, 3, 10, 10));


                for (PartnerItem item : partnerItems.stream().filter(Component::isEnabled).toList()) base.add(item);

                init(contentPane);
            }).start();
        } else {
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getVerticalScrollBar().setUnitIncrement(13);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            contentPane.setLayout(new BorderLayout());
            var border_size = 0;
            contentPane.setBorder(BorderFactory.createEmptyBorder(border_size, border_size, border_size, border_size));

            if (partnerItems.size() == 0 || partnerItems.stream().filter(PartnerItem::isEnabled).toList().size() == 0) {
                JLabel noPartnersLabel = new JLabel(Lang.get("ui.tab.partners.nothing"), SwingConstants.CENTER);
                noPartnersLabel.setForeground(new Color(0xED4245));
                contentPane.add(noPartnersLabel, BorderLayout.CENTER);
                contentPane.repaint();
            } else contentPane.add(scrollPane, BorderLayout.CENTER);
        }
    }

    @Override
    public void tick(int currentTick) {

    }

    private void reloadPartners() {
        if (partnerItems != null) partnerItems.clear();
        else partnerItems = new ArrayList<>();
        var result = Utils.WebUtils.get("https://presence-man.com/api/v1/partners");
        Utils.GSON.fromJson(result.getBody(), JsonArray.class).asList().stream().map(JsonElement::getAsJsonObject).forEach(obj -> {
            try {partnerItems.add(new PartnerItem(obj.get("title").getAsString(), obj.get("about_text").getAsString(), obj.get("domain").getAsString(), obj.get("image").isJsonNull() ? null : new ImageIcon(new URL(obj.get("image").getAsString())), obj.get("banner_image").isJsonNull() ? null : new ImageIcon(new URL(obj.get("banner_image").getAsString())), obj.get("enabled").getAsBoolean(), obj.get("url").isJsonNull() ? null : obj.get("url").getAsString()));} catch (MalformedURLException ignore) {}
        });
        contentPane.validate();
        contentPane.repaint();
    }

    @Override
    public void update() {
        loaded = false;
        init(contentPane);
    }

    public static class PartnerItem extends JPanel {
        public String title;
        public String about_text;
        public String domain;
        public ImageIcon icon;
        public ImageIcon banner_icon;
        public boolean enabled;
        public String url;

        public PartnerItem(JsonObject json) throws MalformedURLException {
            String title = json.get("title").getAsString();
            String domain = json.has("domain") && !json.get("domain").isJsonNull() ? json.get("domain").getAsString() : null;
            String image = json.has("image") && !json.get("image").isJsonNull() ? json.get("image").getAsString() : null;
            var icon = image == null ? null : (image.startsWith("http") ? new ImageIcon(new URL(image)) : new ImageIcon(image));
            String banner_image = json.has("banner_image") && !json.get("banner_image").isJsonNull() ? json.get("banner_image").getAsString() : null;
            var banner_icon = banner_image == null ? null : (banner_image.startsWith("http") ? new ImageIcon(new URL(banner_image)) : new ImageIcon(banner_image));
            boolean enabled = json.has("enabled") && !json.get("enabled").isJsonNull() && json.get("enabled").getAsBoolean();
            String about_text = json.has("about_text") ? json.get("about_text").getAsString() : "No description provided!";
            String url = json.has("url") ? json.get("url").getAsString() : null;
            build(title, about_text, domain, icon, banner_icon, enabled, url);
        }
        public PartnerItem(String title) {build(title, null, null, null, null, true, null);}
        public PartnerItem(String title, String about_text) {build(title, about_text, null, null, null, true, null);}
        public PartnerItem(String title, String about_text, String domain) {build(title, about_text, domain, null, null, true, null);}
        public PartnerItem(String title, String about_text, String domain, ImageIcon icon) {build(title, about_text, domain, icon, null, true, null);}
        public PartnerItem(String title, String about_text, String domain, ImageIcon icon, ImageIcon banner_icon, boolean enabled) {build(title, about_text, domain, icon, banner_icon, enabled, null);}
        public PartnerItem(String title, String about_text, String domain, ImageIcon icon, ImageIcon banner_icon, boolean enabled, String url) {build(title, about_text, domain, icon, banner_icon, enabled, url);}

        private void build(String title, String about_text, String domain, ImageIcon icon, ImageIcon banner_icon, boolean enabled, String url) {
            this.title = title;
            this.about_text = about_text == null ? "No description provided!" : about_text;
            this.domain = domain;
            this.icon = icon;
            this.banner_icon = banner_icon;
            this.enabled = enabled;
            this.url = url;

            if (icon != null) icon = new ImageIcon(icon.getImage().getScaledInstance(icon.getIconWidth() / 4, icon.getIconHeight() / 4, Image.SCALE_SMOOTH));

            setLayout(new GridLayout());

            JButton button = new JButton(title);
            button.setPreferredSize(new Dimension(150,170));
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

        @Override
        public boolean isEnabled() {return enabled;}
    }
}

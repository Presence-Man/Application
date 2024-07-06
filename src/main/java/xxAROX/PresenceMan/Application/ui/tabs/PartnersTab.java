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
import lombok.SneakyThrows;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;
import xxAROX.PresenceMan.Application.Bootstrap;
import xxAROX.PresenceMan.Application.entity.Gateway;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.ui.popup.PartnerPopup;
import xxAROX.PresenceMan.Application.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PartnersTab extends AUITab {
    private List<PartnerItem> partnerItems;

    public PartnersTab(AppUI frame) {
        super(frame, "Partners");
    }
    @Override
    protected void init(JPanel contentPane) {
        try {
            Future<List<PartnerItem>> partners = App.getInstance().getTickExecutor().submit(() -> {
                while (Objects.equals(Gateway.address, "127.0.0.1")) {}
                return fetchPartners();
            });
            while (!partners.isDone()) {}
            partnerItems = partners.get();
        } catch (InterruptedException | ExecutionException ignore) {
        }

        contentPane.setLayout(new BorderLayout());
        var border_size = 0;
        contentPane.setBorder(BorderFactory.createEmptyBorder(border_size, border_size, border_size, border_size));

        JPanel base = new JPanel();
        int itemCount = partnerItems.size();
        if (itemCount == 1) base.setLayout(new GridBagLayout());
        else if (itemCount == 2) base.setLayout(new GridBagLayout());
        else base.setLayout(new GridLayout(0, 3, 10, 10));

        JScrollPane scrollPane = new JScrollPane(base);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(13);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        for (PartnerItem item : partnerItems.stream().filter(Component::isEnabled).toList()) base.add(item);
        contentPane.add(scrollPane, BorderLayout.CENTER);
    }

    @SneakyThrows
    private List<PartnerItem> fetchPartners() {
        var fallback_icon = Objects.requireNonNull(Bootstrap.class.getClassLoader().getResource(AppInfo.icon));
        if (partnerItems == null) partnerItems = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(Gateway.getUrl() + "/api/v1/partners").openStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) content.append(inputLine);
            in.close();
            JsonArray partners = Utils.GSON.fromJson(content.toString(), JsonArray.class);
            ArrayList<PartnerItem> list = new ArrayList<>();
            partners.asList().stream().map(JsonElement::getAsJsonObject).forEach(obj -> {
                try {
                    list.add(new PartnerItem(
                            obj.get("title").getAsString(),
                            obj.get("about_text").getAsString(),
                            obj.get("domain").getAsString(),
                            obj.get("image").isJsonNull() ? null : new ImageIcon(new URL(obj.get("image").getAsString())),
                            obj.get("banner_image").isJsonNull() ? null : new ImageIcon(new URL(obj.get("banner_image").getAsString())),
                            obj.get("enabled").getAsBoolean(),
                            obj.get("url").isJsonNull() ? null : obj.get("url").getAsString()
                        ));
                } catch (MalformedURLException ignore) {
                }
            });
            return list;
        } catch (IOException e) {
            App.getLogger().error("Error while fetching partners: ", e);
        }
        return new ArrayList<>();
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

            if (icon != null) icon = new ImageIcon(icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));

            setLayout(new GridLayout());

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

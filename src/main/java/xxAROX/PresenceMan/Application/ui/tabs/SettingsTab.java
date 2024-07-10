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

import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;
import xxAROX.PresenceMan.Application.utils.CacheManager;
import xxAROX.PresenceMan.Application.utils.Lang;
import xxAROX.PresenceMan.Application.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SettingsTab extends AUITab {
    public SettingsTab(AppUI parent){
        super(parent, "ui.tab.settings", "images/settings.png");
    }

    @Override
    public boolean isScrollable() {
        return true;
    }

    @Override
    protected void init(JPanel contentPane) {
        Map<String, Runnable> before_save = new HashMap<>();
        Map<String, Runnable> after_save = new HashMap<>();

        contentPane.setLayout(new GridBagLayout());
        var margin = 10;
        contentPane.setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(2, 5, 2, 5);
        constraints.weightx = 0.5;
        constraints.ipadx = 10;
        constraints.ipady = 10;
        int gridy = 0;

        //title
        JLabel title = new JLabel(Lang.get("ui.tab.settings.title"));
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 20));
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        contentPane.add(title, constraints);


        Utils.UIUtils.addCheckbox(contentPane, constraints, gridy++, Lang.get("ui.tab.settings.start_minimized.title"), CacheManager.Settings.START_MINIMIZED, (n) -> before_save.put("start-minimized", () -> CacheManager.Settings.START_MINIMIZED = n));

        Utils.UIUtils.addCheckbox(contentPane, constraints, gridy++, Lang.get("ui.tab.settings.auto_update.title"), CacheManager.Settings.ENABLE_AUTO_UPDATE, (n) -> before_save.put("auto-update", () -> CacheManager.Settings.ENABLE_AUTO_UPDATE = n));

        Map<String, String> languages = new HashMap<>();
        AtomicReference<String> default_val = new AtomicReference<>(null);
        Lang.getAvailableLocales().forEach(locale -> {
            var key = Lang.getSpecific(locale, "language.name") + " - " + Lang.get("ui.tab.settings.translated_percentage", new HashMap<>(){{put("{completion}", Lang.getSpecific(locale, "language.completion"));}});
            languages.put(key, locale);
            if (CacheManager.Settings.LOCALE.equals(locale)) default_val.set(key);
        });
        Utils.UIUtils.addDropdownMenu(contentPane, constraints, gridy++, Lang.get("ui.tab.settings.language.title"), languages, default_val.get(), (lang_name, locale) -> {
            before_save.put("locale", () -> CacheManager.Settings.LOCALE = locale);
            after_save.put("locale", () -> {
                try {
                    Utils.launch(Utils.getJarFile().orElseThrow());
                    App.getInstance().shutdown();
                } catch (Throwable e) {
                    App.getLogger().error("Could not start the jar file", e);
                    App.ui.showException(e);
                    System.exit(1);
                }
                App.ui.showInfo(Lang.get("ui.tab.settings.language.success", new HashMap<>(){{
                    put("{language}", Lang.get("language.name"));
                    put("{locale}", locale);
                }}));
            });
        });

        // Save button
        JButton saveButton = new JButton(Lang.get("ui.tab.settings.save"));
        constraints.weightx = 0;
        constraints.weighty = 1.0; // Push to the bottom
        constraints.gridx = 0;
        constraints.gridy = gridy;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.SOUTHEAST;
        constraints.fill = GridBagConstraints.NONE;
        saveButton.addActionListener(e -> {
            saveButton.setFocusPainted(false);
            for (Map.Entry<String, Runnable> entry : before_save.entrySet()) entry.getValue().run();
            CacheManager.save();
            for (Map.Entry<String, Runnable> entry : after_save.entrySet()) entry.getValue().run();
        });
        saveButton.setForeground(new Color(0, 0, 0));
        saveButton.setFocusable(false);
        saveButton.setBackground(new Color(0, 128, 0));


        contentPane.add(saveButton, constraints);
    }
}

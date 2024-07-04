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
import xxAROX.PresenceMan.Application.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsTab extends AUITab {
    public SettingsTab(AppUI parent){
        super(parent, "Settings");
    }

    @Override
    protected void init(JPanel contentPane) {
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        AtomicBoolean changed = new AtomicBoolean(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 0.5;
        constraints.ipadx = 20;
        constraints.ipady = 10;
        int gridy = 0;


        Utils.UIUtils.addCheckbox(contentPane, constraints, gridy++, "Start minimized", CacheManager.Settings.START_MINIMIZED, (n) -> {
            changed.set(CacheManager.Settings.START_MINIMIZED != n);
            CacheManager.Settings.START_MINIMIZED = n;
        });
        Utils.UIUtils.addCheckbox(contentPane, constraints, gridy++, "Enable auto-update", CacheManager.Settings.ENABLE_AUTO_UPDATE, (n) -> {
            changed.set(CacheManager.Settings.ENABLE_AUTO_UPDATE != n);
            CacheManager.Settings.ENABLE_AUTO_UPDATE = n;
        });

        // Save button
        JButton saveButton = new JButton("Save settings");
        constraints.gridx = 0;
        constraints.gridy = gridy;
        constraints.gridwidth = 2;
        saveButton.addActionListener(e -> {
            if (changed.get()) {
                CacheManager.save();
                App.ui.showInfo("Successfully saved settings!");
            }
        });
        contentPane.add(saveButton, constraints);
    }
}

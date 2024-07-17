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

package xxAROX.PresenceMan.Application.ui;

import lombok.Getter;
import lombok.ToString;
import xxAROX.PresenceMan.Application.utils.Lang;
import xxAROX.PresenceMan.Application.utils.Utils;

import javax.swing.*;

@Getter
@ToString
public abstract class AUITab {
    protected final AppUI frame;
    protected final String name;
    protected ImageIcon icon;
    private final String tip;
    protected final JPanel contentPane;

    public AUITab(AppUI frame, String name) {
        this(frame, name, null, null);
    }
    public AUITab(AppUI frame, String name, String icon) {
        this(frame, name, icon, null);
    }
    public AUITab(AppUI frame, String name, String icon, String tip) {
        this.frame = frame;
        this.icon = icon == null ? null : Utils.UIUtils.createImageIcon(icon);
        if (this.icon != null) {
            this.icon = new ImageIcon(this.icon.getImage());
            tip = name;
            name = null;
        }
        this.name = name;
        this.tip = tip != null ? Lang.get(tip) : null;
        contentPane = new JPanel();
        contentPane.setLayout(null);
        init(contentPane);
    }


    public void add(final JTabbedPane tabbedPane) {
        if (isScrollable()) {
            JScrollPane scrollPane = new JScrollPane(contentPane);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getVerticalScrollBar().setUnitIncrement(13);
            tabbedPane.addTab(name, icon, scrollPane, tip);
        } else tabbedPane.addTab(name, icon, contentPane, tip);
    }

    protected abstract void init(final JPanel contentPane);

    public void tick(int currentTick) {
    }

    public boolean isScrollable() {
        return false;
    }

    public void update() {
    }

    public void setReady() {
    }

    public void onClose() {
    }
}

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

import javax.swing.*;

@Getter
@ToString
public abstract class AUITab {
    protected final AppUI frame;
    protected final String name;
    private final String tip;
    protected final JPanel contentPane;

    public AUITab(AppUI frame, String name) {
        this(frame, name, null);
    }
    public AUITab(AppUI frame, String name, String tip) {
        this.frame = frame;
        this.name = name;
        this.tip = tip;
        contentPane = new JPanel();
        contentPane.setLayout(null);
        init(contentPane);
    }


    public void add(final JTabbedPane tabbedPane) {
        tabbedPane.addTab(this.name, null, this.contentPane, tip);
    }

    protected abstract void init(final JPanel contentPane);

    public void tick(int currentTick) {
    }

    public void update() {}

    public void setReady() {
    }

    public void onClose() {
    }
}

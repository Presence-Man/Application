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
import xxAROX.PresenceMan.Application.Bootstrap;
import xxAROX.PresenceMan.Application.ui.AUITab;
import xxAROX.PresenceMan.Application.ui.AppUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class PrivacyPolicyTab extends AUITab {
    public PrivacyPolicyTab(AppUI parent){
        super(parent, "Privacy policy");
    }

    @Override
    protected void init(JPanel contentPane) {
        contentPane.setLayout(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JEditorPane privacyPolicyPane = new JEditorPane();
        privacyPolicyPane.setEditable(false);
        privacyPolicyPane.setContentType("text/html");

        JScrollPane scrollPane = new JScrollPane(privacyPolicyPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        try {
            InputStream inputStream = Bootstrap.class.getClassLoader().getResourceAsStream("privacy_policy.html");
            assert inputStream != null : "privacy_policy.html resource not found!";
            privacyPolicyPane.setText("<html>\n" + new String(inputStream.readAllBytes()) + "\n</html>");
        } catch (IOException e) {
            App.getInstance().getLogger().error(e);
        }
        contentPane.add(scrollPane, BorderLayout.CENTER);
    }
}

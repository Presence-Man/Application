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
        super(parent, "Privacy policy", "What are we doing with your data?");
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
            privacyPolicyPane.setText(new String(inputStream.readAllBytes()));
        } catch (IOException e) {
            App.getInstance().getLogger().error(e);
        }
        contentPane.add(scrollPane, BorderLayout.CENTER);
    }
}

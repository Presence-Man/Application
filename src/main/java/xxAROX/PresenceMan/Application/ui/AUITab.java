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
    private final JPanel contentPane;

    public AUITab(AppUI frame, String name, String tip) {
        this.frame = frame;
        this.name = name;
        this.tip = tip;
        contentPane = new JPanel();
        contentPane.setLayout(null);
        init(contentPane);
    }

    public AUITab(AppUI frame, String name) {
        this(frame, name, null);
    }

    public void add(final JTabbedPane tabbedPane) {
        tabbedPane.addTab(this.name, null, this.contentPane, tip);
    }

    protected abstract void init(final JPanel contentPane);

    public void setReady() {
    }

    public void onClose() {
    }
}

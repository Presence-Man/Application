package xxAROX.PresenceMan.Application.scheduler;


import lombok.SneakyThrows;
import xxAROX.PresenceMan.Application.App;

import javax.swing.*;
import java.io.IOException;

public abstract class Task implements Runnable {
    private TaskHandler<Task> handler;

    public abstract void onRun(int currentTick) throws IOException;

    public abstract void onCancel();

    public void onError(Throwable error) {
        if (App.ui != null) SwingUtilities.invokeLater(() -> App.ui.showException(error));
    }

    @SneakyThrows
    @Override
    public void run() {
        this.onRun(this.handler.getLastRunTick());
    }

    public int getTaskId() {
        return this.handler == null ? -1 : this.handler.getTaskId();
    }

    public void cancel() {
        this.handler.cancel();
    }

    public TaskHandler<Task> getHandler() {
        return this.handler;
    }

    public void setHandler(TaskHandler<Task> handler) {
        if (this.handler != null) throw new SecurityException("Can not change task handler!");
        this.handler = handler;
    }
}

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

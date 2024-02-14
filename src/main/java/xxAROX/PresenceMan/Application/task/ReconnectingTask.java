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

package xxAROX.PresenceMan.Application.task;

import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.entity.Gateway;
import xxAROX.PresenceMan.Application.scheduler.Task;
import xxAROX.PresenceMan.Application.scheduler.TaskHandler;

public class ReconnectingTask extends Task {
    private static boolean active = false;
    private static TaskHandler<ReconnectingTask> task = null;

    public static void activate(){
        if (active) return;
        active = true;
        task = App.getInstance().getScheduler().scheduleRepeating(new ReconnectingTask(), 20 * 5);
    }

    public static void deactivate(){
        if (!active) return;
        task.cancel();
        task = null;
        active = false;
    }

    @Override
    public void onRun(int currentTick) {
        FetchGatewayInformationTask.ping_backend();
        if (!Gateway.broken) App.getLogger().info("Reconnected to backend!");
    }

    @Override
    public void onCancel() {
    }
}

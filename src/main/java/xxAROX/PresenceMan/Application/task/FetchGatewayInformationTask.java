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

import com.google.gson.JsonObject;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.entity.Gateway;
import xxAROX.PresenceMan.Application.scheduler.Task;
import xxAROX.PresenceMan.Application.utils.Utils;

public class FetchGatewayInformationTask extends Task {
    private static final String URL = "https://raw.githubusercontent.com/Presence-Man/Gateway/main/gateway.json";

    @Override
    public void onRun(int currentTick) {
        String content = Utils.WebUtils.get(URL).getBody();
        JsonObject gateway = Utils.GSON.fromJson(content, JsonObject.class);

        Gateway.protocol = gateway.get("protocol").getAsString();
        Gateway.ip = gateway.get("ip").getAsString();
        Gateway.address = gateway.get("address").getAsString();
        Gateway.port = gateway.has("port") && !gateway.get("port").isJsonNull() ? gateway.get("port").getAsInt() : null;
        Gateway.usual_port = gateway.has("usual_port") && !gateway.get("usual_port").isJsonNull() ? gateway.get("usual_port").getAsInt() : 15151;

        App.getLogger().info("Got gateway data!");
        ping_backend();
    }

    public static void ping_backend() {
        var response = Utils.WebUtils.get(Gateway.getUrl());
        var result = response != null && response.getBody().toLowerCase().contains("jan sohn / xxarox");
        if (result) {
            ReconnectingTask.deactivate();
            Gateway.broken = false;
            App.getInstance().initSocket();
        } else {
            Gateway.broken = true;
            ReconnectingTask.activate();
        }
    }

    @Override
    public void onCancel() {
    }
}

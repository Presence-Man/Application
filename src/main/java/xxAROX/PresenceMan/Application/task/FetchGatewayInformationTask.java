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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.entity.Gateway;
import xxAROX.PresenceMan.Application.scheduler.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class FetchGatewayInformationTask extends Task {
    private static final String URL = "https://raw.githubusercontent.com/Presence-Man/Gateway/main/gateway.json";

    @Override
    public void onRun(int currentTick) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(URL).openStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) content.append(inputLine);
            in.close();
            JsonObject gateway = new Gson().fromJson(content.toString(), JsonObject.class);

            Gateway.protocol = gateway.get("protocol").getAsString();
            Gateway.ip = gateway.get("ip").getAsString();
            Gateway.address = gateway.get("address").getAsString();
            Gateway.port = gateway.has("port") && !gateway.get("port").isJsonNull() ? gateway.get("port").getAsInt() : null;
            Gateway.usual_port = gateway.has("usual_port") && !gateway.get("usual_port").isJsonNull() ? gateway.get("usual_port").getAsInt() : 15151;

            App.getLogger().info("Got gateway information!");
            ping_backend();
        } catch (IOException e) {
            App.getLogger().error("Error while fetching gateway information: ", e);
        }
    }

    public static void ping_backend() {
        boolean result = false;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(Gateway.getUrl()).openStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) content.append(inputLine);
            in.close();
            result = content.toString().toLowerCase().contains("jan sohn / xxarox");
        } catch (IOException ignore) {
        }
        Gateway.broken = result;
        if (result) {
            ReconnectingTask.deactivate();
            Gateway.broken = false;
            Gateway.broken_popup = false;
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

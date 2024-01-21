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

package xxAROX.PresenceMan.Application;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import xxAROX.PresenceMan.Application.entity.APIActivity;
import xxAROX.PresenceMan.Application.entity.Gateway;
import xxAROX.PresenceMan.Application.sockets.SocketThread;
import xxAROX.PresenceMan.Application.sockets.protocol.packets.types.HeartbeatPacket;
import xxAROX.PresenceMan.Application.task.ReconnectingTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class RestAPI {
    private static volatile boolean pending_heartbeat = false;
    public static class Endpoints {
        public static String skin = "/api/v1/images/skins/";
        public static String head = "/api/v1/images/heads/";
    }
    public static String heartbeat(){
        if (SocketThread.getInstance() != null && SocketThread.getInstance().getSession_token() == null) return null;
        //if (pending_heartbeat) return null;
        if (App.getInstance().socket == null) return null;
        if (App.getInstance().xboxUserInfo == null) return null;

        pending_heartbeat = true;
        System.out.println(App.getInstance().socket.sendPacket(new HeartbeatPacket(), (pk) -> {
            System.out.println(8);
            pending_heartbeat = false;
            if (App.getInstance().featuredServer != null) return;
            System.out.println(9);
            String network = pk.getNetwork();
            String before_network = App.getInstance().network;

            if (before_network == null || !before_network.equalsIgnoreCase(network)) {
                App.network_session_created = Instant.now().toEpochMilli();
                App.getInstance().network = network;
            }
            System.out.println(10);

            String server = pk.getServer();
            String before_server = App.getInstance().server;
            if (before_server == null || !before_server.equalsIgnoreCase(server)) {
                App.server_session_created = Instant.now().toEpochMilli();
                App.getInstance().server = server;
            }
            System.out.println(11);

            APIActivity new_activity = pk.getApi_activity();
            if (new_activity == null) new_activity = APIActivity.none();
            if (new_activity.equals(App.getInstance().getApi_activity())) return;
            App.setActivity(new_activity);
            System.out.println(12);
        }, err -> {
            App.getInstance().network = null;
            App.getInstance().server = null;
            App.getLogger().error("Error on heartbeat: " + err);
        }));
        return null;
    }


    private static JsonObject request(@NonNull String endpoint) {
        return request(Method.GET, endpoint, new HashMap<>(), new JsonObject(), new HashMap<>());
    }
    private static JsonObject request(@NonNull Method method, @NonNull String endpoint) {
        return request(method, endpoint, new HashMap<>(), new JsonObject(), new HashMap<>());
    }
    private static JsonObject request(@NonNull Method method, @NonNull String endpoint, @NonNull Map<String, String> query) {
        return request(method, endpoint, query, new JsonObject(), new HashMap<>());
    }
    private static JsonObject request(@NonNull Method method, @NonNull String endpoint, @NonNull Map<String, String> query, @NonNull JsonObject body) {
        return request(method, endpoint, query, body, new HashMap<>());
    }
    private static JsonObject request(@NonNull Method method, @NonNull String endpoint, @NonNull Map<String, String> query, @NonNull JsonObject body, @NonNull Map<String, String> headers) {
        if (Gateway.broken && !Gateway.broken_popup) {
            Gateway.broken_popup = true;
            App.ui.showError("Backend server is unreachable, please try again later!\n<html><i>If this happens often please contact @xx_arox on Discord!</i></html>");
            ReconnectingTask.activate();
            return null;
        }
        try {
            URL url = new URL(Gateway.getUrl() + endpoint + getParamsString(query));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method.toString());
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            for (Map.Entry<String, String> entry : headers.entrySet()) con.setRequestProperty(entry.getKey(), entry.getValue());
            if (!method.equals(Method.GET)) {
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                    os.flush();
                } catch (IOException ignore) {
                }
            }
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setInstanceFollowRedirects(true);

            int status = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(status > 299 ? con.getErrorStream() : con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) content.append(inputLine);
            in.close();
            con.disconnect();
            return new Gson().fromJson(content.toString(), JsonObject.class);
        } catch (JsonSyntaxException ignore) {
        } catch (IOException e) {
            if (Gateway.connected) Gateway.connected = false;
            if (!Gateway.broken) Gateway.broken = true;
        }
        return null;
    }

    public static String getParamsString(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }
        String resultString = result.toString();
        return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
    }

    @AllArgsConstructor
    public enum Method {
        GET("GET"),
        POST("POST"),
        DELETE("DELETE"),
        ;
        private String method;

        @Override
        public String toString() {
            return method.toUpperCase();
        }
    }
}

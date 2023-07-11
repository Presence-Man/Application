package xxAROX.PresenceMan.Application.task;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;
import xxAROX.PresenceMan.Application.entity.APIActivity;
import xxAROX.PresenceMan.Application.entity.XboxUserInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RestAPI {
    private static APIActivity activity;
    private static String ip;
    private static volatile boolean broken = false;

    static {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream()));
            ip = in.readLine(); //you get the IP as a String
        } catch (IOException e) {
            App.ui.showError("This app doesn't work in offline mode!");
            System.exit(0);
        }
    }

    public static void heartbeat(){
        if (App.getInstance().getXboxUserInfo() == null) return;
        JsonObject body = new JsonObject();
        JsonObject response = request(Method.POST, "/user/heartbeat", new HashMap<>(), body);
        if (response == null) return;
        APIActivity new_activity = null;
        if (!response.has("api_activity")) {
            new_activity = APIActivity.none();
            if (activity != null) new_activity.setStart(activity.getStart());
        } else if (response.get("api_activity").isJsonObject()) {
            new_activity = APIActivity.deserialize(response.get("api_activity").getAsJsonObject());
        }
        if (!Objects.equals(new_activity, activity)) {
            activity = new_activity;
            App.getDiscord_core().activityManager().updateActivity(activity.toDiscord());
        }
        System.out.println(response);
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
        System.out.println(AppInfo.Backend.protocol + AppInfo.Backend.address + ":" + AppInfo.Backend.port + endpoint + getParamsString(query));
        if (broken) {
            App.ui.showError("Backend server is unreachable, please try again later!\n<html><i>If this happens often please contact @xx_arox on Discord!</i></html>");
            App.getInstance().shutdown();
            return null;
        }
        try {
            URL url = new URL(AppInfo.Backend.protocol + AppInfo.Backend.address + ":" + AppInfo.Backend.port + endpoint + getParamsString(query));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method.toString());
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            for (Map.Entry<String, String> entry : headers.entrySet()) con.setRequestProperty(entry.getKey(), entry.getValue());
            if (!method.equals(Method.GET)) {
                con.setDoOutput(true);
                XboxUserInfo xboxUserInfo = App.getInstance().getXboxUserInfo();
                if (xboxUserInfo != null) {
                    body.addProperty("xuid", xboxUserInfo.getXuid());
                    body.addProperty("gamertag", xboxUserInfo.getGamertag());
                }
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
        } catch (IOException e) {
            broken = true;
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

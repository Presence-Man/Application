package xxAROX.PresenceMan.Application.task;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.RestAPI;
import xxAROX.PresenceMan.Application.entity.Gateway;
import xxAROX.PresenceMan.Application.scheduler.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class FetchGatewayInformationTask extends Task {
    private static final String URL = "https://raw.githubusercontent.com/Presence-Man/releases/main/gateway.json";

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

            System.out.println("Got gateway information!");
            ping_backend();
        } catch (IOException e) {
            System.out.println("Error while fetching gateway information: ");
            e.printStackTrace();
        }
        App.getInstance().getScheduler().scheduleRepeating(() -> {
            String result = RestAPI.heartbeat();
            if (result != null) System.out.println("Heartbeat result: " + result);
            App.ui.general_tab.tick();
        }, 20 * 5);
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
            System.out.println("Connected to backend successfully!");
            App.getInstance().initSocket();
        } else {
            Gateway.broken = true;
            System.out.println("Couldn't connect to backend successfully, reconnecting..");
            ReconnectingTask.activate();
        }
    }

    @Override
    public void onCancel() {
    }
}

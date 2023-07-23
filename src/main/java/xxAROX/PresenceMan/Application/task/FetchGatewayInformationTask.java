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
            Gateway.address = gateway.get("address").getAsString();
            Gateway.port = gateway.get("port").getAsInt();
            ping_backend();
        } catch (IOException e) {
            System.out.println("Error while fetching gateway information: ");
            e.printStackTrace();
        }
        App.getInstance().getScheduler().scheduleRepeating(() -> {
            RestAPI.heartbeat();
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
            result = content.toString().toLowerCase().contains("presence-man");
        } catch (IOException ignore) {
        }
        Gateway.broken = result;
        if (result) {
            ReconnectingTask.deactivate();
            Gateway.broken = false;
            Gateway.broken_popup = false;
        } else {
            Gateway.broken = true;
            ReconnectingTask.activate();
        }
    }

    @Override
    public void onCancel() {
    }
}

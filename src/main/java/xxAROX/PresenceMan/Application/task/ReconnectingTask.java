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
        if (!Gateway.broken) System.out.println("Reconnected to backend!");
    }

    @Override
    public void onCancel() {
    }
}

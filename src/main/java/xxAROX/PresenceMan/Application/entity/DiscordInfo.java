package xxAROX.PresenceMan.Application.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString @Getter @Setter
public class DiscordInfo {
    public boolean ready = false;
    private final List<Runnable> ready_handlers = new ArrayList<>();
    private volatile String discord_user_id = null;
    private volatile String current_application_id = "null";

    public void registerHandler(Runnable runnable){
        if (ready) runnable.run();
        else {
            ready_handlers.add(runnable);
            checkHandlers();
        }
    }
    public void checkHandlers(){
        if (ready && ready_handlers.size() > 0) {
            List<Runnable> for_deletion = new ArrayList<>();
            for (Runnable run : ready_handlers) {
                run.run();
                for_deletion.add(run);
            }
            for (Runnable run : for_deletion) ready_handlers.remove(run);
        }
    }
}

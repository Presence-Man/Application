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

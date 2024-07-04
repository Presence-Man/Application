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

package xxAROX.PresenceMan.Application.utils;

import org.apache.logging.log4j.Logger;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    private static Map<String, String> getDefaultParams() {
        return new HashMap<>(){{
            put("{App.name}", AppInfo.name);
            put("{App.version}", AppInfo.getVersion());

            if (App.getInstance().xboxUserInfo != null) {
                put("{xuid}", App.getInstance().xboxUserInfo.getXuid());
                put("{gamertag}", App.getInstance().xboxUserInfo.getGamertag());
            }
            put("{network}", App.getInstance().network != null ? App.getInstance().network : "null");
            put("{server}", App.getInstance().server != null ? App.getInstance().server : "null");

        }};
    }

    public static String replaceParams(String base){
        return replaceParams(base, getDefaultParams());
    }
    public static String replaceParams(String base, Map<String, String> params){
        if (!params.containsKey("{App.name}")) params.putAll(getDefaultParams());
        for (Map.Entry<String, String> keyValueEntry : params.entrySet()) base = base.replace(keyValueEntry.getKey(), keyValueEntry.getValue());
        return base;
    }

    public static class SingleInstanceUtils {
        private static final String LOCK_FILE = System.getProperty("user.home") + "/.presence-man.lock";

        public static boolean hook(Logger logger) {
            if (isAppAlreadyRunning(logger)) return false;
            Runtime.getRuntime().addShutdownHook(new Thread(SingleInstanceUtils::releaseLock));
            return true;
        }
        private static boolean isAppAlreadyRunning(Logger logger) {
            try {
                File file = new File(LOCK_FILE);
                if (file.exists()) return true;
                if (!file.createNewFile()) return true;
                file.deleteOnExit();
            } catch (IOException e) {
                logger.error(e);
                return true;
            }
            return false;
        }
        private static void releaseLock() {
            File file = new File(LOCK_FILE);
            if (file.exists()) file.delete();
        }
    }
}

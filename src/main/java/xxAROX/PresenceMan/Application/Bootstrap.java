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

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xxAROX.PresenceMan.Application.utils.Utils;

import java.util.Arrays;
import java.util.List;

public class Bootstrap {
    @SneakyThrows
    public static void main(String[] _args) {
        List<String> args = Arrays.stream(_args).toList();
        List<String> lowArgs = args.stream().map(String::toLowerCase).toList();

        AppInfo.development = lowArgs.contains("dev") || lowArgs.contains("development") || lowArgs.contains("alpha");
        AppInfo.alpha = lowArgs.contains("alpha");

        Logger logger = initializeLogger();
        if (Utils.SingleInstanceUtils.lockInstance(logger)) new App(logger);
        else {
            logger.error("Application is already running, shutting down this one..");
            System.exit(1);
        }
    }

    protected static Logger initializeLogger(){
        //PropertyConfigurator.configure(ClassLoader.getSystemResource("log4j.properties"));
        var logger = LogManager.getLogger(App.class);
        //logger.setLevel(AppInfo.development ? Level.DEBUG : Level.INFO); // TODO @KeksDev fix meee
        return logger;
    }
}

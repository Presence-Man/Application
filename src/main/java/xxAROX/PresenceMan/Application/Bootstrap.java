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
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Arrays;
import java.util.List;

@Log4j
public class Bootstrap {
    @SneakyThrows
    public static void main(String[] _args) {
        List<String> args = Arrays.stream(_args).toList();
        List<String> lowArgs = args.stream().map(String::toLowerCase).toList();

        AppInfo.development = lowArgs.contains("dev") || lowArgs.contains("development");

        Logger logger = initializeLogger();
        new App(logger);
    }

    protected static Logger initializeLogger(){
        PropertyConfigurator.configure(ClassLoader.getSystemResource("log4j.properties"));
        var logger = Logger.getLogger(App.class);
        logger.setLevel(AppInfo.development ? Level.DEBUG : Level.INFO);
        return logger;
    }
}

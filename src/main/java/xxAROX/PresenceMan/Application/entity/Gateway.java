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

public class Gateway {
    public static String protocol = "http://";
    public static String ip = "37.114.55.239";
    public static String address = "127.0.0.1";
    public static Integer port = 15151;
    public static Integer usual_port = 15151;

    public static boolean connected = false;
    public static boolean broken = false;
    public static boolean broken_popup = false;

    public static String getUrl() {
        return protocol + address + (port == null ? "" : ":" + port);
    }
}

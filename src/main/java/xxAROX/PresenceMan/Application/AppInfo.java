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

import java.util.StringJoiner;

public final class AppInfo {
    public final static int[] version = new int[]{ 1,2,1 };
    public final static String name = "Presence-Man";
    public static String icon = "icon.png";
    public static long discord_application_id = 1133823892486623344L;
    public static boolean development = false;

    public static String getVersion(){
        return getVersion(".");
    }

    public static String getVersion(CharSequence delimiter){
        StringJoiner joiner = new StringJoiner(delimiter);
        for (int part : version) joiner.add(String.valueOf(part));
        return joiner.toString();
    }

    public static void main(String[] args) {
        System.out.println(getVersion()); // NOTE: DO NOT REMOVE
    }
}

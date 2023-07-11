package xxAROX.PresenceMan.Application;

import xxAROX.PresenceMan.Application.task.RestAPI;

import java.util.StringJoiner;

public final class AppInfo {
    public final static String name = "Presence-Man";
    public final static int[] version = new int[]{ 0,1,0 };
    public static String icon = "icon.png";
    public static String discord_application_id = "1127704366565052526";

    public static String getVersion(){
        return getVersion(".");
    }

    public static String getVersion(CharSequence delimiter){
        StringJoiner joiner = new StringJoiner(delimiter);
        for (int part : version) joiner.add(String.valueOf(part));
        return joiner.toString();
    }

    public static void main(String[] args) {
        System.out.println(getVersion());
    }

    public final static class Backend {
        public final static RestAPI.Protocol protocol = RestAPI.Protocol.HTTP;
        public final static String address = "127.0.0.1";
        public final static int port = 1515;
    }
}

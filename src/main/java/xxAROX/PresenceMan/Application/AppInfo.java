package xxAROX.PresenceMan.Application;

import java.util.StringJoiner;

public final class AppInfo {
    public final static String name = "Presence-Man";
    public final static int[] version = new int[]{ 1,1,1s };
    public static String icon = "icon.png";
    public static long discord_application_id = 1133823892486623344L;

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
}

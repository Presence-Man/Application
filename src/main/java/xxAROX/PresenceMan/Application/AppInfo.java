package xxAROX.PresenceMan.Application;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.StringJoiner;

public final class AppInfo {
    public final static String name = "Presence-Man";
    public final static InetSocketAddress address = InetSocketAddress.createUnresolved("exoniamc.de", 1515);
    public final static int[] version = new int[]{ 0,1,0 };
    public static String icon = "icon.png";
    public final static String[] contributors = new String[]{
            "xxAROX",
    };
    public static String discord_application_id = "345229890980937739";

    public static String getVersion(){
        return getVersion(".");
    }

    public static String getVersion(CharSequence delimiter){
        StringJoiner joiner = new StringJoiner(delimiter);
        for (int part : version) joiner.add(String.valueOf(part));
        return joiner.toString();
    }

    public static String getContributors(){
        return getContributors(", ");
    }

    public static String getContributors(CharSequence delimiter){
        return String.join(delimiter, contributors);
    }

    public static void main(String[] args) {
        var json = """
                {
                    "name": {name},
                    "version": {version},
                    "contributors": {contributors}
                }
                         """;
        System.out.println(json
                .replace("{name}", name)
                .replace("{version}", Arrays.toString(version))
                .replace("{contributors}", "[\"" + String.join("\", \"", contributors) + "\"]")
        );
    }
}

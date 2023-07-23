package xxAROX.PresenceMan.Application.entity;

public class Gateway {
    public static String protocol = "http://";
    public static String address = "127.0.0.1";
    public static int port = 15151;
    public static boolean broken = false;
    public static boolean broken_popup = false;

    public static String getUrl() {
        return protocol + address + ":" + port;
    }
}

package xxAROX.PresenceMan.Application;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;

public class Bootstrap {
    @SneakyThrows
    public static void main(String[] args) {
        if (!isJVMOk()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, 
                    new JLabel("<html><b>Error<b>: Unsupported JVM: \""
                        + System.getProperty("java.vm.name", "Undef") + "\" "
                        + System.getProperty("java.vm.version", "Undef")
                        + "<br>The JVM version required to run is 17<br>"
                ), "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            });
            return;
        }
        new App();
    }
    protected static void shutdownHook() {
        LogManager.shutdown();
        Runtime.getRuntime().halt(0); // force exit
    }
    public static boolean isJVMOk(){
        boolean result = false;
        String s = System.getProperty("java.version", "undef");
        if (!s.equals("undef")) {
            String parts[] = s.split("[^0-9]+");
            if (parts.length >= 2) {
                if (parts[0].equals("1") && parts[1].compareTo("6") >= 0) {
                    result = true;
                }
            }
        }
        return result;
    }
}

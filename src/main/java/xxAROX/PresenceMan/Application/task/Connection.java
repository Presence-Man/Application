package xxAROX.PresenceMan.Application.task;

import lombok.Getter;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Getter
public final class Connection implements Runnable {
    @Getter
    private static Connection instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Connection(){
        new Thread(this).start();
    }




    @Override
    public void run() {
        try {
            System.out.println(AppInfo.address.getAddress());
            socket = new Socket(AppInfo.address.getAddress(), AppInfo.address.getPort());
            out = new PrintWriter(socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("Hello from " + socket.getLocalSocketAddress());
            String line = in.readLine();
            System.out.println("Received: '" + line + "' from Backend-Server");
        } catch (IOException e) {
            App.ui.showException(e);
        }
        while (socket != null && socket.isConnected()) {
            // NOTE: keeping this thread alive
        }
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();

            socket = null;
            out = null;
            in = null;
        } catch (IOException e) {
            // IGNORE
        }
    }
}

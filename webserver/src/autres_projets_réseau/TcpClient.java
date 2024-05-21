import java.io.*;
import java.net.*;

public class TcpClient {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage : java TcpClient <adresse_serveur> <port>");
            System.exit(1);
        }

        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);

        Socket socket = new Socket(serverAddress, port);
        System.out.println("[Client] Connexion au serveur " + serverAddress + " sur le port " + port + " ...");

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String message;
        while (true) {
            message = in.readLine();
            if ("stop".equals(message)) {
                System.out.println("[Client] Message 'stop' reçu, arrêt du programme ...");
                break;
            }
            System.out.println("[Client] Message reçu : " + message);

            System.out.println("[Client] Entrez un message : ");
            message = System.console().readLine();
            out.println(message);
        }

        socket.close();
    }
}

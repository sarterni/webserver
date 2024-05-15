import java.io.*;
import java.net.*;

public class TcpServer {

    private static final int DEFAULT_PORT = 12345;

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Numéro de port invalide : " + args[0]);
                System.exit(1);
            }
        }

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("[Serveur] En attente d'une connexion sur le port " + port + " ...");
        Socket socket = serverSocket.accept();
        System.out.println("[Serveur] Connexion établie avec " + socket.getRemoteSocketAddress());

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String message;
        while (true) {
            System.out.println("[Serveur] Attente d'un message au clavier ...");
            message = System.console().readLine();
            out.println(message);

            message = in.readLine();
            if ("stop".equals(message)) {
                System.out.println("[Serveur] Message 'stop' reçu, arrêt du programme ...");
                break;
            }
            System.out.println("[Serveur] Message reçu : " + message);
        }

        socket.close();
        serverSocket.close();
    }
}

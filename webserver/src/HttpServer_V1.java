import java.io.*;
import java.net.*;
import java.nio.file.*;


public class HttpServer_V1 {
    public static void main(String[] args) {
        int port = 80; // Default port
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number, using default port 80.");
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return;
        }

        System.out.println("Received: " + requestLine);
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 3 || !requestParts[0].equals("GET")) {
            sendResponse(out, "HTTP/1.1 400 Bad Request\r\n\r\n");
            return;
        }

        String filePath = requestParts[1];
        if (filePath.equals("/")) {
            filePath = "/index.html";
        }

        filePath = "." + filePath;

        File file = new File(filePath);
        if (file.exists() && !file.isDirectory()) {
            String mimeType = Files.probeContentType(file.toPath());
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            sendResponse(out, "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + mimeType + "\r\n" +
                    "Content-Length: " + fileBytes.length + "\r\n" +
                    "\r\n");
            out.write(fileBytes);
        } else {
            sendResponse(out, "HTTP/1.1 404 Not Found\r\n\r\n");
        }
    }

    private static void sendResponse(OutputStream out, String header) throws IOException {
        out.write(header.getBytes());
        out.flush();
    }
}

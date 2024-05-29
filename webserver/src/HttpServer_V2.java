import java.io.*;
import java.net.*;
import java.nio.file.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.concurrent.*;

public class HttpServer_V2 {
    private static final int DEFAULT_PORT = 80;
    private static final int THREAD_POOL_SIZE = 10;
    private static final String DEFAULT_ROOT = "./"; // Default root directory

    public static void main(String[] args) {
        int port = readPortFromXmlConfig("webconfig.xml");
        String rootPath = readRootFromXmlConfig("webconfig.xml");

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            System.out.println("Root path: " + rootPath);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleClient(clientSocket, rootPath));
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    private static int readPortFromXmlConfig(String filePath) {
        try {
            File file = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            Element portElement = (Element) doc.getElementsByTagName("port").item(0);
            return Integer.parseInt(portElement.getTextContent());
        } catch (Exception e) {
            System.err.println(
                    "Error reading port from config, using default port " + DEFAULT_PORT + ": " + e.getMessage());
            return DEFAULT_PORT;
        }
    }

    private static String readRootFromXmlConfig(String filePath) {
        try {
            File file = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            Element rootElement = (Element) doc.getElementsByTagName("root").item(0);
            return rootElement.getTextContent();
        } catch (Exception e) {
            System.err.println(
                    "Error reading root from config, using default root " + DEFAULT_ROOT + ": " + e.getMessage());
            return DEFAULT_ROOT;
        }
    }

    private static void handleClient(Socket clientSocket, String rootPath) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream()) {

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

            filePath = rootPath + filePath;

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
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private static void sendResponse(OutputStream out, String header) throws IOException {
        out.write(header.getBytes());
        out.flush();
    }
}

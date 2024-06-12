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
        File logFile = new File("error.log"); // Step 2: Create or open the log file

        try (ServerSocket serverSocket = new ServerSocket(port);
             PrintWriter logWriter = new PrintWriter(new FileWriter(logFile, true))) { // Open the log file for writing
            System.out.println("Server started on port " + port);
            System.out.println("Root path: " + rootPath);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleClient(clientSocket, rootPath));
            }
        } catch (IOException e) {
            try (PrintWriter logWriter = new PrintWriter(new FileWriter(logFile, true))) { // Open the log file in catch block
                logWriter.println("Could not start server: " + e.getMessage()); // Step 3: Write the error message to log file
            } catch (IOException logException) {
                System.err.println("Could not write to log file: " + logException.getMessage());
            }
        } finally {
            threadPool.shutdown();
            // No need to close PrintWriter here since it's auto-closed by try-with-resources
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
        // Déclaration du FileWriter en dehors du try pour pouvoir le fermer dans le finally
        FileWriter fw = null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream()) {
    
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }
    
            // Ouvrir le fichier access.log en mode append
            fw = new FileWriter("access.log", true);
            fw.write("Received: " + requestLine + "\n"); // Écrire la ligne de requête dans access.log
    
            System.out.println("Received: " + requestLine);
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 3 || !requestParts[0].equals("GET")) {
                sendResponse(out, "HTTP/1.1 400 Bad Request\r\n\r\n");
                return;
            }
    
            String filePath = requestParts[1];
            // Plus de logique de gestion de la requête ici...
        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        } finally {
            if (fw != null) {
                try {
                    fw.close(); // Assurer la fermeture du FileWriter
                } catch (IOException e) {
                    System.err.println("Error closing FileWriter: " + e.getMessage());
                }
            }
        }
    }
    
    private static void sendResponse(OutputStream out, String response) throws IOException {
        out.write(response.getBytes());
        out.flush();
    }}
import java.io.*;
import java.net.*;
import java.nio.file.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Base64;
import java.util.concurrent.*;
import java.lang.management.ManagementFactory; // Add this import statement

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
                PrintWriter logWriter = new PrintWriter(new FileWriter(logFile, true))) { // Open the log file for
                                                                                          // writing
            System.out.println("Server started on port " + port);
            System.out.println("Root path: " + rootPath);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleClient(clientSocket, rootPath));
            }
        } catch (IOException e) {
            try (PrintWriter logWriter = new PrintWriter(new FileWriter(logFile, true))) { // Open the log file in catch
                                                                                           // block
                logWriter.println("Could not start server: " + e.getMessage()); // Step 3: Write the error message to
                                                                                // log file
            } catch (IOException logException) {
                System.err.println("Could not write to log file: " + logException.getMessage());
            }
        } finally {
            threadPool.shutdown();
            // No need to close PrintWriter here since it's auto-closed by
            // try-with-resources
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
        // Déclaration du FileWriter en dehors du try pour pouvoir le fermer dans le
        // finally
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
            if (filePath.equals("/")) {
                filePath = "/index.html";
            }

            else if (requestParts[1].equals("/status")) {
                String status = "Server Status:\n";
                Runtime runtime = Runtime.getRuntime();
                long freeMemory = runtime.freeMemory();
                long diskSpace = new File("/").getUsableSpace();
                int processCount = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
                status += "Free Memory: " + freeMemory + " bytes\n";
                status += "Disk Space: " + diskSpace + " bytes\n";
                status += "Process Count: " + processCount + "\n";
                sendResponse(out, "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + status.length() + "\r\n" +
                        "\r\n" +
                        status);
            
                // Add code to write status to status.html
                String statusFilePath = rootPath + "/status.html"; // Declare the filePath variable
                File statusFile = new File(statusFilePath);
                try (PrintWriter writer = new PrintWriter(statusFile)) {
                    writer.println("<html>");
                    writer.println("<head><title>Server Status</title></head>");
                    writer.println("<body>");
                    writer.println("<h1>Server Status</h1>");
                    writer.println("<p>Free Memory: " + freeMemory + " bytes</p>");
                    writer.println("<p>Disk Space: " + diskSpace + " bytes</p>");
                    writer.println("<p>Process Count: " + processCount + "</p>");
                    writer.println("</body>");
                    writer.println("</html>");
                } catch (IOException e) {
                    System.err.println("Error writing status to status.html: " + e.getMessage());
                }
            }
            filePath = rootPath + filePath;
            
            File file = new File(filePath);
            if (file.exists() && !file.isDirectory()) {
                // Après avoir vérifié que le fichier existe et n'est pas un répertoire
                String mimeType = Files.probeContentType(file.toPath());
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                sendResponse(out, "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + fileBytes.length + "\r\n" +
                        "\r\n");
                out.write(fileBytes);
            
                // Vérifier si le fichier est de type image, son, ou vidéo
                // if (mimeType.startsWith("image/") || mimeType.startsWith("audio/") ||
                // mimeType.startsWith("video/")) {
                // // Encoder le contenu du fichier en base64
                // String base64Content = Base64.getEncoder().encodeToString(fileBytes);
            
                // // Envoyer les en-têtes avec Content-Encoding: base64 et le type MIME
                // approprié
                // sendResponse(out, "HTTP/1.1 200 OK\r\n" +
                // "Content-Type: " + mimeType + "\r\n" +
                // "Content-Encoding: base64\r\n" +
                // "Content-Length: " + base64Content.length() + "\r\n" +
                // "\r\n" +
                // base64Content);
                // } else {
                // // Envoyer le fichier normalement si ce n'est pas un fichier image, son, ou
                // // vidéo
                // sendResponse(out, "HTTP/1.1 200 OK\r\n" +
                // "Content-Type: " + mimeType + "\r\n" +
                // "Content-Length: " + fileBytes.length + "\r\n" +
                // "\r\n");
                // out.write(fileBytes);
                // }
            } else {
                sendResponse(out, "HTTP/1.1 404 Not Found\r\n\r\n");
            }
            
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
            }
            }

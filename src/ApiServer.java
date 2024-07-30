import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ApiServer {

    private static final Logger logger = Logger.getLogger(ApiServer.class.getName());
    private static final String UPLOAD_DIR = "uploads";

    public static void main(String[] args) throws IOException {
        // Ensure upload directory exists
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        logger.info("Starting the server...");
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/api/uploadPhoto", new UploadPhotoHandler());
        server.createContext("/api/uploadMetadata", new UploadMetadataHandler());
        server.createContext("/api/photos/", new PhotoActionHandler());
        server.createContext("/api/photos", new ListPhotosHandler());
        server.createContext("/api/photos/search", new SearchPhotosHandler());
        server.setExecutor(null);
        server.start();
        logger.info("Server started on port 8000");
        try {
            DatabasePool.create();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    static class ListPhotosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                logger.info("Received request to list photos");
                String response = "List of all photos";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                logger.info("Listed all photos successfully");
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                logger.warning("Received non-GET request to /photos");
            }
        }
    }
}

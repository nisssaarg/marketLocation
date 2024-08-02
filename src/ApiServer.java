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

    private static final String SERVER_STARTED_ON_PORT_8000 = "Server started on port 8000";
    private static final String API_PHOTOS_SEARCH = "/api/photos/search";
    private static final String API_PHOTOS2 = "/api/photos";
    private static final String API_PHOTOS = "/api/photos/";
    private static final String API_UPLOAD_METADATA = "/api/uploadMetadata";
    private static final String API_UPLOAD_PHOTO = "/api/uploadPhoto";
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
        server.createContext(API_UPLOAD_PHOTO, new UploadPhotoHandler());
        server.createContext(API_UPLOAD_METADATA, new UploadMetadataHandler());
        server.createContext(API_PHOTOS, new PhotoActionHandler());
        server.createContext(API_PHOTOS2, new ListPhotosHandler());
        server.createContext(API_PHOTOS_SEARCH, new SearchPhotosHandler());
        server.setExecutor(null);
        server.start();
        logger.info(SERVER_STARTED_ON_PORT_8000);
        try {
            DatabasePool.getInstance(); // Initialize the connection pool
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

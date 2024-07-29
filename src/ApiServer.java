import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;
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
    }

    static class PhotoActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            logger.info("Received request to perform action on photo: " + path);
            String response;
            if (path.matches("/api/photos/\\d+/like")) {
                String photoId = extractPhotoId(path, "/like");
                response = "Like posted for photo id: " + photoId;
                logger.info("Like action performed on photo id: " + photoId);
            } else if (path.matches("/api/photos/\\d+/buy")) {
                String photoId = extractPhotoId(path, "/buy");
                response = "Photo bought with id: " + photoId;
                logger.info("Buy action performed on photo id: " + photoId);
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
                logger.warning("Invalid action request: " + path);
                return;
            }
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String extractPhotoId(String path, String action) {
            return path.substring("/api/photos/".length(), path.length() - action.length());
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

        private Map<String, String> parseQueryParams(String query) {
            Map<String, String> queryParams = new HashMap<>();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        queryParams.put(pair[0], pair[1]);
                    }
                }
            }
            return queryParams;
        }
    }

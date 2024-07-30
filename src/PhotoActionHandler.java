import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class PhotoActionHandler implements HttpHandler {

    private static final String INVALID_ACTION_REQUEST = "Invalid action request: ";
    private static final String RECEIVED_REQUEST_TO_PERFORM_ACTION_ON_PHOTO = "Received request to perform action on photo: ";
    private static final Logger logger = Logger.getLogger(ApiServer.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        logger.info(RECEIVED_REQUEST_TO_PERFORM_ACTION_ON_PHOTO + path);
        
        if (path.matches("/api/photos/\\d+/like")) {
            handleLikeAction(exchange);
        } else if (path.matches("/api/photos/\\d+/buy")) {
        } else {
            sendResponse(exchange, 404, createJsonResponse("error", "Invalid action request"));
            logger.warning(INVALID_ACTION_REQUEST + path);
        }
    }

    private void handleLikeAction(HttpExchange exchange) throws IOException {
        String photoId = extractPhotoId(exchange.getRequestURI().getPath(), "/like");
        try {
            RedisLikes.likePost(photoId);
            long likeCount = RedisLikes.getLikes(photoId);
            ObjectNode responseJson = objectMapper.createObjectNode()
                .put("success", true)
                .put("message", "Like posted for photo id: " + photoId)
                .put("likeCount", likeCount);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(responseJson));
            logger.info("Like action performed on photo id: " + photoId);
        } catch (Exception e) {
            logger.severe("Error liking photo: " + e.getMessage());
            sendResponse(exchange, 500, createJsonResponse("error", "Failed to like photo"));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String createJsonResponse(String status, String message) throws IOException {
        ObjectNode jsonNode = objectMapper.createObjectNode()
            .put("status", status)
            .put("message", message);
        return objectMapper.writeValueAsString(jsonNode);
    }

    private String extractPhotoId(String path, String action) {
        return path.substring("/api/photos/".length(), path.length() - action.length());
    }
}
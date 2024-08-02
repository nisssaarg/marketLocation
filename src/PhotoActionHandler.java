import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class PhotoActionHandler implements HttpHandler {

    private static final String API_PHOTOS = "/api/photos/";
    private static final String STATUS2 = "status";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String FAILED_TO_LIKE_PHOTO = "Failed to like photo";
    private static final String ERROR_LIKING_PHOTO = "Error liking photo: ";
    private static final String LIKE_ACTION_PERFORMED_ON_PHOTO_ID = "Like action performed on photo id: ";
    private static final String LIKE_COUNT = "likeCount";
    private static final String LIKE_POSTED_FOR_PHOTO_ID = "Like posted for photo id: ";
    private static final String MESSAGE = "message";
    private static final String SUCCESS = "success";
    private static final String INVALID_ACTION_REQUEST2 = "Invalid action request";
    private static final String ERROR = "error";
    private static final String LIKE = "/like";
    private static final String API_PHOTOS_D_BUY = "/api/photos/\\d+/buy";
    private static final String API_PHOTOS_D_LIKE = "/api/photos/\\d+/like";
    private static final String INVALID_ACTION_REQUEST = "Invalid action request: ";
    private static final String RECEIVED_REQUEST_TO_PERFORM_ACTION_ON_PHOTO = "Received request to perform action on photo: ";
    private static final Logger logger = Logger.getLogger(ApiServer.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        logger.info(RECEIVED_REQUEST_TO_PERFORM_ACTION_ON_PHOTO + path);
        
        if (path.matches(API_PHOTOS_D_LIKE)) {
            handleLikeAction(exchange);
        } else if (path.matches(API_PHOTOS_D_BUY)) {
        } else {
            sendResponse(exchange, 404, createJsonResponse(ERROR, INVALID_ACTION_REQUEST2));
            logger.warning(INVALID_ACTION_REQUEST + path);
        }
    }

    private void handleLikeAction(HttpExchange exchange) throws IOException {
        String photoId = extractPhotoId(exchange.getRequestURI().getPath(), LIKE);
        try {
            RedisLikes.likePost(photoId);
            long likeCount =RedisLikes.getLikes(photoId);
            //CassandraLikes.likePost(photoId);
            //System.out.println(CassandraLikes.getLikes(photoId));
            ObjectNode responseJson = objectMapper.createObjectNode()
                .put(SUCCESS, true)
                .put(MESSAGE, LIKE_POSTED_FOR_PHOTO_ID + photoId)
                .put(LIKE_COUNT, likeCount);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(responseJson));
            logger.info(LIKE_ACTION_PERFORMED_ON_PHOTO_ID + photoId);
        } catch (Exception e) {
            logger.severe(ERROR_LIKING_PHOTO + e.getMessage());
            sendResponse(exchange, 500, createJsonResponse(ERROR, FAILED_TO_LIKE_PHOTO));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set(CONTENT_TYPE, APPLICATION_JSON);
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String createJsonResponse(String status, String message) throws IOException {
        ObjectNode jsonNode = objectMapper.createObjectNode()
            .put(STATUS2, status)
            .put(MESSAGE, message);
        return objectMapper.writeValueAsString(jsonNode);
    }

    private String extractPhotoId(String path, String action) {
        return path.substring(API_PHOTOS.length(), path.length() - action.length());
    }
}
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ListPhotosHandler implements HttpHandler {

    private static final Logger logger = Logger.getLogger(ListPhotosHandler.class.getName());
    private static final String SELECT_RANDOM_PHOTOS_QUERY = 
        "SELECT M.metadata_id, H.photo_path, COALESCE(L.likecount, 0) AS likecount " + 
        "FROM METADATA M " +
        "JOIN HASH H ON M.hash_id = H.hash_id " +
        "LEFT JOIN LIKES L ON M.metadata_id = L.metadata_id " +
        "ORDER BY RANDOM() " +
        "LIMIT 10;";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            logger.info("Received request to list photos");

            StringBuilder response = new StringBuilder();
            try (Connection connection = DatabasePool.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(SELECT_RANDOM_PHOTOS_QUERY)) {

                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    int metadataId = resultSet.getInt("metadata_id");
                    String photoPath = resultSet.getString("photo_path");
                    String likes = resultSet.getString("likecount");
                    // Append metadata_id and photo_path to the response
                    response.append(metadataId).append(",").append(photoPath).append(",").append(likes).append("\n");
                }
            } catch (SQLException e) {
                logger.severe("Failed to retrieve photos: " + e.getMessage());
                exchange.sendResponseHeaders(500, -1); // Internal Server Error
                return;
            }

            // Send response
            String responseBody = response.toString();
            exchange.sendResponseHeaders(200, responseBody.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseBody.getBytes());
            os.close();
            logger.info("Listed photos successfully");
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            logger.warning("Received non-GET request to /photos");
        }
    }
}

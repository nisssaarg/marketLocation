import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class DeleteHandler implements HttpHandler {

    private static final String INVALID_REQUEST = "Invalid request";
    private static final String 
    PHOTO_NOT_FOUND = "Photo not found";
    private static final String PHOTO_DELETED_SUCCESSFULLY = "Photo deleted successfully";
    private static final String PHOTOID2 = "photoid";
    private static final String REGEX = "=";
    private static final String DELETE = "DELETE";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (DELETE.equalsIgnoreCase(exchange.getRequestMethod())) {
            // Extract photoid from the request URI
            String query = exchange.getRequestURI().getQuery();
            String[] params = query.split(REGEX);
            String photoid = null;

            if (params.length == 2 && PHOTOID2.equals(params[0])) {
                photoid = params[1];
            }

            if (photoid != null) {
                // Implement your photo deletion logic here
                boolean isDeleted = DeleteDatabaseHelper.deletePhoto(photoid);

                if (isDeleted) {
                    String response = PHOTO_DELETED_SUCCESSFULLY;
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    String response = PHOTO_NOT_FOUND;
                    exchange.sendResponseHeaders(404, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                String response = INVALID_REQUEST;
                exchange.sendResponseHeaders(400, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
}

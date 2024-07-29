import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Map;
import java.util.logging.Logger;

class UploadMetadataHandler implements HttpHandler {

    // todo - fix copy paste error below
    private static final Logger logger = Logger.getLogger(ApiServer.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            logger.info("Received request to upload metadata");
            InputStream is = exchange.getRequestBody();
            String json = new String(is.readAllBytes());

            // Parse the JSON metadata
            Map<String, Object> metadata;
            try {
                metadata = objectMapper.readValue(json, Map.class);
                logger.info("Parsed metadata: " + metadata.toString());

                // Process the metadata (e.g., store it in a database)
                MetadataWriter writer = new MetadataWriter();
                writer.writeToDatabase(metadata);

                String response = "Uploaded metadata successfully";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                logger.info("Metadata uploaded successfully");
            } catch (IOException e) {
                logger.severe("Failed to parse metadata: " + e.getMessage());
                exchange.sendResponseHeaders(400, -1); // Bad Request
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            logger.warning("Received non-POST request to /uploadMetadata");
        }
    }
}

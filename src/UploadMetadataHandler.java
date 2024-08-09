import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.HashMap;
// /import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;

class UploadMetadataHandler implements HttpHandler {

    private static final String RECEIVED_NON_POST_REQUEST_TO_UPLOAD_METADATA = "Received non-POST request to /uploadMetadata";
    private static final String FAILED_TO_PARSE_METADATA = "Failed to parse metadata: ";
    private static final String METADATA_UPLOADED_SUCCESSFULLY = "Metadata uploaded successfully";
    private static final String PARSED_METADATA = "Parsed metadata: ";
    private static final String RECEIVED_REQUEST_TO_UPLOAD_METADATA = "Received request to upload metadata";
    private static final String UPLOADED_METADATA_SUCCESSFULLY = "Uploaded metadata successfully";
    private static final Logger logger = Logger.getLogger(UploadMetadataHandler.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            logger.info(RECEIVED_REQUEST_TO_UPLOAD_METADATA);
            InputStream is = exchange.getRequestBody();
            String json = new String(is.readAllBytes());
            Queue queue = new Queue();
            Map<String, String> rs = new HashMap<>();
            // Parse the JSON metadata
            Map<String, Object> metadata;
            try {
                metadata = objectMapper.readValue(json, Map.class);
                logger.info(PARSED_METADATA + metadata.toString());
                // Process the metadata (e.g., store it in a database)
                MetadataWriter writer = new MetadataWriter();
                rs = writer.writeToDatabase(metadata);
                String response = UPLOADED_METADATA_SUCCESSFULLY;
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                logger.info(METADATA_UPLOADED_SUCCESSFULLY);
            } catch (IOException e) {
                logger.severe(FAILED_TO_PARSE_METADATA + e.getMessage());
                exchange.sendResponseHeaders(400, -1); // Bad Request
            }
            finally{
                if(rs != null){
                    logger.info("Queueing");
                    queue.enqueue(rs);
                }
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            logger.warning(RECEIVED_NON_POST_REQUEST_TO_UPLOAD_METADATA);
        }
    }
}

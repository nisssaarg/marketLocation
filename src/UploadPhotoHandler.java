import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Logger;

public class UploadPhotoHandler implements HttpHandler {
    private static final String RECEIVED_NON_POST_REQUEST_TO_UPLOAD_PHOTO = "Received non-POST request to /uploadPhoto";
    private static final String PHOTO_UPLOAD_FAILED = "Photo upload failed: ";
    private static final String FILE_UPLOAD_FAILED = "File upload failed.";
    private static final String PHOTO_UPLOADED_SUCCESSFULLY = "Photo uploaded successfully: ";
    private static final String RECEIVED_REQUEST_TO_UPLOAD_PHOTO = "Received request to upload photo";
    private static final Logger logger = Logger.getLogger(UploadPhotoHandler.class.getName());
    private static final String UPLOAD_DIR = "uploads";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            logger.info(RECEIVED_REQUEST_TO_UPLOAD_PHOTO);
            logger.info("Thread id: " + Thread.currentThread().getId());
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            InputStream is = exchange.getRequestBody();
            byte[] fileBytes = is.readAllBytes();
            
            boolean success = false;
            String hash = ImageHashGenerator.generateImageHash(fileBytes);
            String isExists = DuplicateChecker.checkDuplicates(hash);
            String filename;
            Path filePath;
            FileUploader upload = new FileUploader();

            if (isExists == null) {
                filename = System.currentTimeMillis() + "_uploaded_photo.jpg";
                filePath = Paths.get(UPLOAD_DIR, filename);
                success = upload.uploadFile(filePath, fileBytes);
                
                // Save hash and photo_path in the HASH table
                MetadataWriter writer = new MetadataWriter();
                writer.savePhotoPath(hash, filePath.toString());

                filename = filePath.toString();
            } else {
                filename = isExists;
                success = true;
            }

            String jsonResponse = "";
            if (success) {
                jsonResponse = new ObjectMapper().writeValueAsString(Map.of("filename", filename, "hash", hash));
                logger.info(jsonResponse);
                exchange.sendResponseHeaders(200, jsonResponse.length());
            } else {
                jsonResponse = FILE_UPLOAD_FAILED;
                exchange.sendResponseHeaders(500, jsonResponse.length());
            }
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes());
            }
            logger.info(success ? PHOTO_UPLOADED_SUCCESSFULLY + filename : PHOTO_UPLOAD_FAILED + filename);
        } else {
            exchange.sendResponseHeaders(405, -1);
            logger.warning(RECEIVED_NON_POST_REQUEST_TO_UPLOAD_PHOTO);
        }
    }
}


import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class UploadPhotoHandler implements HttpHandler {
    private static final String RECEIVED_NON_POST_REQUEST_TO_UPLOAD_PHOTO = "Received non-POST request to /uploadPhoto";
    private static final String PHOTO_UPLOAD_FAILED = "Photo upload failed: ";
    private static final String FILE_UPLOAD_FAILED = "File upload failed.";
    private static final String PHOTO_UPLOADED_SUCCESSFULLY = "Photo uploaded successfully: ";
    private static final String RECEIVED_REQUEST_TO_UPLOAD_PHOTO = "Received request to upload photo";
    private static final Logger logger = Logger.getLogger(UploadPhotoHandler.class.getName());
    private static final String UPLOAD_DIR = "uploads"; // Directory to save uploaded files

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            logger.info(RECEIVED_REQUEST_TO_UPLOAD_PHOTO);
            logger.info("Thread id: " + Thread.currentThread().getId());
            // Create upload directory if it doesn't exist
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            InputStream is = exchange.getRequestBody();
            // todo - lower priority - think of how to avoid this
            byte[] fileBytes = is.readAllBytes();
            
            // Create a unique filename
            // todo - make it thread safe
            String filename = System.currentTimeMillis() + "_uploaded_photo.jpg";
            Path filePath = Paths.get(UPLOAD_DIR, filename);

            FileUploader upload = new FileUploader();
            // Save the uploaded file
            // todo - get rid of filename param
            boolean success = upload.uploadFile(filePath,fileBytes);
            if(success){
                String response = filePath.toString();
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                logger.info(PHOTO_UPLOADED_SUCCESSFULLY + filename);
            }else{
                String response = FILE_UPLOAD_FAILED;
                exchange.sendResponseHeaders(500, response.length()); // Internal Server Error
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                logger.severe(PHOTO_UPLOAD_FAILED + filename);
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            logger.warning(RECEIVED_NON_POST_REQUEST_TO_UPLOAD_PHOTO);
        }
    }
}

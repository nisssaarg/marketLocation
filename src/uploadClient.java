import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

public class uploadClient {

    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String POST = "POST";
    private static final String STARTING_PHOTO_UPLOAD_FOR = "Starting photo upload for: ";
    private static final String FILE_NOT_FOUND = "File not found: ";
    private static final String UPLOAD_PHOTO_URL = "http://localhost:8000/api/uploadPhoto";
    private static final String UPLOAD_METADATA_URL = "http://localhost:8000/api/uploadMetadata";
    private static final Logger logger = Logger.getLogger(uploadClient.class.getName());

    static {
        setupLogger();
    }

    private static void setupLogger() {
        try {
            // Create a FileHandler
            FileHandler fileHandler = new FileHandler("uploader.log", true);
            
            // Create a SimpleFormatter
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            // Remove existing handlers (to avoid console output)
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            // Add the FileHandler to the logger
            logger.addHandler(fileHandler);

            // Set the logging level
            logger.setLevel(Level.ALL);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> uploadPhoto(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            logger.severe(FILE_NOT_FOUND + filePath);
            return new HashMap<>(); // Return an empty map in case of failure
        }

        Map<String, String> responseMap = new HashMap<>();
        Instant startTime = Instant.now();
        logger.info(STARTING_PHOTO_UPLOAD_FOR + filePath);

        HttpURLConnection connection = null;
        DataOutputStream dos = null;

        try {
            byte[] fileBytes = convertFileToByteArray(file); // Convert file to byte array

            URL url = new URL(UPLOAD_PHOTO_URL);
            connection = (HttpURLConnection) url.openConnection();

            // Configure the connection
            connection.setDoOutput(true);
            connection.setRequestMethod(POST);
            connection.setRequestProperty(CONTENT_TYPE, APPLICATION_OCTET_STREAM);

            // Send the file bytes
            dos = new DataOutputStream(connection.getOutputStream());
            dos.write(fileBytes);
            dos.flush();
            logger.info("File uploaded successfully!");

            // Get the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String responseLine;
                    StringBuilder response = new StringBuilder();
                    
                    while ((responseLine = in.readLine()) != null) {
                        response.append(responseLine);
                    }
                    
                    ObjectMapper objectMapper = new ObjectMapper();
                    responseMap = objectMapper.readValue(response.toString(), new TypeReference<Map<String, String>>(){});
                    
                    logger.info("Server response: " + responseMap);
                }
            } else {
                logger.severe("Failed to upload file. Server responded with code: " + responseCode);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error uploading file: " + filePath, e);
        } finally {
            if (dos != null) {
                dos.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
            Instant endTime = Instant.now();
            long timeTaken = Duration.between(startTime, endTime).toMillis();
            logger.info("Time taken to upload photo: " + timeTaken + " ms");
        }

        return responseMap;
    }


    public static void uploadMetadata(String location, String subject, String season, String[] keywords,String hash_id, String hash, String photo_path) {
        Instant startTime = Instant.now();
        //logger.info("Starting metadata upload for photo: " + uploadPath);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadMetadata = new HttpPost(UPLOAD_METADATA_URL);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("location", location);
            metadata.put("subject", subject);
            metadata.put("season", season);
            metadata.put("hash_id" , Integer.valueOf(hash_id));
            metadata.put("photo_path", photo_path);
            metadata.put("hash", hash);

            for (int i = 0; i < keywords.length; i++) {
                metadata.put("keyword" + (i + 1), keywords[i]);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(metadata);

            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            uploadMetadata.setEntity(entity);

            logger.info("Executing metadata upload request: " + uploadMetadata.getRequestLine());

            try (CloseableHttpResponse response = httpClient.execute(uploadMetadata)) {
                int statusCode = response.getStatusLine().getStatusCode();
                logger.info("Metadata Upload Response Code: " + statusCode);
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.info("Metadata Upload Response Body: " + responseBody);

                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("Metadata uploaded successfully.");
                } else {
                    logger.severe("Failed to upload metadata. Status code: " + statusCode);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred during metadata upload", e);
        } finally {
            Instant endTime = Instant.now();
            long timeTaken = Duration.between(startTime, endTime).toMillis();
            logger.info("Time taken to upload metadata: " + timeTaken + " ms");
        }
    }

    private static byte[] convertFileToByteArray(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    public static void main(String[] args) throws IOException {
        // Example usage
        String filePath = "/Users/nisssaarg/Desktop/9697.jpg";
        Map<String, String> uploadResponse = uploadPhoto(filePath);
        String uploadPath = uploadResponse.get("filename");
        String hash = uploadResponse.get("hash");

        // Metadata to be uploaded
        String location = "Example Location";
        String subject = "Example Subject";
        String season = "Summer";
        String[] keywords = {"keyword1", "keyword2"};

        //uploadMetadata(location, subject, season, keywords);
    }
}

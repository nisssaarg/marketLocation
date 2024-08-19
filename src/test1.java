import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;

public class test1 {

    private static final String UPLOAD_PHOTO_URL = "http://localhost:8000/api/uploadPhoto";
    private static final String UPLOAD_METADATA_URL = "http://localhost:8000/api/uploadMetadata";
    private static final Logger logger = Logger.getLogger(test1.class.getName());

    // Reuse a single instance of CloseableHttpClient
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    public static String uploadPhoto(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            logger.severe("File not found: " + filePath);
            return "";
        }
    
        String uploadPath = "";
        synchronized (httpClient) { // Synchronizing on httpClient to ensure thread safety
            HttpPost uploadPhotoRequest = new HttpPost(UPLOAD_PHOTO_URL);
    
            try {
                // Set the file as the entity
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] fileBytes = new byte[(int) file.length()];
                fileInputStream.read(fileBytes);
                fileInputStream.close();
    
                StringEntity entity = new StringEntity(new String(fileBytes), ContentType.APPLICATION_OCTET_STREAM);
                uploadPhotoRequest.setEntity(entity);
    
                logger.info("Executing photo upload request: " + uploadPhotoRequest.getRequestLine());
    
                try (CloseableHttpResponse response = httpClient.execute(uploadPhotoRequest)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    logger.info("Photo Upload Response Code: " + statusCode);
    
                    if (statusCode >= 200 && statusCode < 300) {
                        uploadPath = EntityUtils.toString(response.getEntity());
                        logger.info("Photo uploaded successfully. Server response: " + uploadPath);
                    } else {
                        logger.severe("Failed to upload photo. Status code: " + statusCode);
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "An error occurred during photo upload", e);
            }
        }
        return uploadPath;
    }
    

    public static void uploadMetadata(String location, String subject, String season, String[] keywords, String uploadPath) {
        HttpPost uploadMetadata = new HttpPost(UPLOAD_METADATA_URL);

        Map<String, Object> metadata = new HashMap<>();
        // todo - move constants
        metadata.put("location", location);
        metadata.put("subject", subject);
        metadata.put("season", season);
        metadata.put("photo_path", uploadPath);

        for (int i = 0; i < keywords.length; i++) {
            metadata.put("keyword" + (i + 1), keywords[i]);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(metadata);

            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            uploadMetadata.setEntity(entity);

            logger.info("Executing metadata upload request: " + uploadMetadata.getRequestLine());

            synchronized (httpClient) { // Synchronize on the httpClient to ensure thread safety
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
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred during metadata upload", e);
        }
    }

    private static final String IMAGE_FOLDER_PATH = "/Users/nisssaarg/Desktop/car";
    private static final int THREAD_COUNT = 5; // Number of threads to use

    // Sample data for randomization
    private static final String[] LOCATIONS = {"New York", "Paris", "Tokyo", "London", "Berlin"};
    private static final String[] SUBJECTS = {"Nature", "Urban", "Portrait", "Abstract", "Wildlife"};
    private static final String[] SEASONS = {"Spring", "Summer", "Autumn", "Winter"};
    private static final String[][] KEYWORDS = {
        {"sunset", "landscape"},
        {"cityscape", "night"},
        {"portrait", "model"},
        {"wildlife", "animals"},
        {"abstract", "colors"}
    };

    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        File folder = new File(IMAGE_FOLDER_PATH);
        File[] imageFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png");
            }
        });

        if (imageFiles != null) {
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
            for (File imageFile : imageFiles) {
                executorService.submit(() -> {
                    System.out.println("Thread ID: " + Thread.currentThread().getId());
                    String uploadPath = test1.uploadPhoto(imageFile.getAbsolutePath());
                    logger.info(imageFile.getAbsolutePath());
                    if (!uploadPath.isEmpty()) {
                        // Randomize metadata
                        String location = LOCATIONS[RANDOM.nextInt(LOCATIONS.length)];
                        String subject = SUBJECTS[RANDOM.nextInt(SUBJECTS.length)];
                        String season = SEASONS[RANDOM.nextInt(SEASONS.length)];
                        String[] keywords = KEYWORDS[RANDOM.nextInt(KEYWORDS.length)];
                        logger.info("Entry" + location + " " + subject +" ");

                        test1.uploadMetadata(location, subject, season, keywords, imageFile.getAbsolutePath());
                    }
                });
            }
            executorService.shutdown();
        } else {
            System.out.println("No image files found in the specified folder.");
        }
    }
}

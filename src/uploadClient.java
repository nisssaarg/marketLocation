import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class uploadClient {

    private static final String UPLOAD_PHOTO_URL = "http://localhost:8000/api/uploadPhoto";
    private static final String UPLOAD_METADATA_URL = "http://localhost:8000/api/uploadMetadata";
    private static final Logger logger = Logger.getLogger(uploadClient.class.getName());

    public static void main(String[] args) {
        String filePath = "/Users/nisssaarg/Downloads/photomarketplace.png";

        // Metadata
        String location = "SanFrancisco";
        String subject = "Goldengate";
        String season = "Summer";
        String[] keywords = {"Nature", "Park", "Trees", "Bridge"};

        try {
            // Upload photo and retrieve the upload path
            String uploadPath = uploadPhoto(filePath);

            // Upload metadata only if photo upload was successful
            if (!uploadPath.isEmpty()) {
                uploadMetadata(location, subject, season, keywords, uploadPath);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred during file upload", e);
        }
    }

    private static String uploadPhoto(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            logger.severe("File not found: " + filePath);
            return "";
        }

        HttpURLConnection connection = null;
        DataOutputStream dos = null;
        String uploadPath = "";

        try (FileInputStream fis = new FileInputStream(file)) {
            URL url = new URL(UPLOAD_PHOTO_URL);
            connection = (HttpURLConnection) url.openConnection();

            // Configure the connection
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/octet-stream");

            // Send the file
            dos = new DataOutputStream(connection.getOutputStream());
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }

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
                    uploadPath = response.toString();
                    logger.info("Server response: " + uploadPath);
                }
            } else {
                logger.severe("Failed to upload file. Server responded with code: " + responseCode);
            }
        } finally {
            if (dos != null) {
                dos.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return uploadPath;
    }

    private static void uploadMetadata(String location, String subject, String season, String[] keywords, String uploadPath) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
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
        }
    }
}

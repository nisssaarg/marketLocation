import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LikePhotosClient {

    private static final Logger logger = Logger.getLogger(LikePhotosClient.class.getName());
    private static final String LIST_PHOTOS_URL = "http://localhost:8000/api/photos"; // Update with the actual server URL

    public static void main(String[] args) {
        try {
            LikePhotosClient client = new LikePhotosClient();
            client.listPhotos();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to list photos", e);
        }
    }

    public void listPhotos() throws IOException {
        URL url = new URL(LIST_PHOTOS_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Each line contains metadata_id and photo_path
                    String[] parts = inputLine.split(",", 2); // Split only at the first comma
                    if (parts.length == 2) {
                        String metadataId = parts[0];
                        String photoPath = parts[1];
                        System.out.println("Metadata ID: " + metadataId);
                        System.out.println("Photo Path: " + photoPath);
                    }
                }
                logger.info("Photos retrieved successfully.");
            }
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            logger.warning("No photos found.");
        } else {
            logger.warning("Received error response: " + responseCode);
        }
    }
}

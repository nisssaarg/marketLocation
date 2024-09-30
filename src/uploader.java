import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class uploader {

    private static final String IMAGE_FOLDER_PATH = "/Users/nisssaarg/Desktop/demo";
    private static final int THREAD_COUNT = 5; // Number of threads to use
    private static final Logger logger = Logger.getLogger(uploader.class.getName());

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
        File[] imageFiles = folder.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

        if (imageFiles != null && imageFiles.length > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
            for (File imageFile : imageFiles) {
                executorService.submit(() -> uploadImage(imageFile));
            }
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.MINUTES)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        } else {
            logger.warning("No image files found in the specified folder.");
        }
    }

    private static void uploadImage(File imageFile) {
        long startTime = System.currentTimeMillis();
        logger.info("Starting upload for: " + imageFile.getName());

        try {
            Map<String, String> uploadResponse = uploadClient.uploadPhoto(imageFile.getAbsolutePath());
            if (uploadResponse != null && !uploadResponse.isEmpty()) {
                long uploadDuration = System.currentTimeMillis() - startTime;
                logger.info("Upload completed for: " + imageFile.getName() + " in " + uploadDuration + " ms");

                String hash_id = uploadResponse.get("hash_id");
                String hash = uploadResponse.get("hash");
                String photo_path = uploadResponse.get("filename");

                // Randomize metadata
                String location = LOCATIONS[RANDOM.nextInt(LOCATIONS.length)];
                String subject = SUBJECTS[RANDOM.nextInt(SUBJECTS.length)];
                String season = SEASONS[RANDOM.nextInt(SEASONS.length)];
                String[] keywords = KEYWORDS[RANDOM.nextInt(KEYWORDS.length)];

                long metadataStartTime = System.currentTimeMillis();
                uploadClient.uploadMetadata(location, subject, season, keywords, hash_id, hash, photo_path);
                long metadataDuration = System.currentTimeMillis() - metadataStartTime;
                logger.info("Metadata upload completed for: " + imageFile.getName() + " in " + metadataDuration + " ms");

                long totalDuration = System.currentTimeMillis() - startTime;
                logger.info("Total processing time for: " + imageFile.getName() + " is " + totalDuration + " ms");
            } else {
                logger.warning("Upload path is null or empty for: " + imageFile.getName());
            }
        } catch (IOException e) {
            logger.severe("Error uploading " + imageFile.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}

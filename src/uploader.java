import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class uploader {
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
                    try {
                        String uploadPath = uploadClient.uploadPhoto(imageFile.getAbsolutePath());
                        if (!uploadPath.isEmpty()) {
                            // Randomize metadata
                            String location = LOCATIONS[RANDOM.nextInt(LOCATIONS.length)];
                            String subject = SUBJECTS[RANDOM.nextInt(SUBJECTS.length)];
                            String season = SEASONS[RANDOM.nextInt(SEASONS.length)];
                            String[] keywords = KEYWORDS[RANDOM.nextInt(KEYWORDS.length)];

                            uploadClient.uploadMetadata(location, subject, season, keywords, uploadPath);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            executorService.shutdown();
        } else {
            System.out.println("No image files found in the specified folder.");
        }
    }
}

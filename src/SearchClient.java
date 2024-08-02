import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SearchClient {
    private static final String EXTRACTING_FILE = "Extracting file: ";
    private static final String RESPONSE_CODE = "Response Code: ";
    private static final String STR2 = "&";
    private static final String STR = "=";
    private static final String REQUEST_URL = "Request URL: ";
    private static final String CREATED_DIRECTORY = "Created directory: ";
    private static final String HTTP_GET_REQUEST_FAILED_WITH_RESPONSE_CODE = "HTTP GET request failed with response code: ";
    private static final String GET = "GET";
    private static final String FILES_EXTRACTED_SUCCESSFULLY = "Files extracted successfully.";
    private static final String BASE_URL = "http://localhost:8000"; // Adjust as needed
    private static final String SEARCH_ENDPOINT = "/api/photos/search";

    public void searchPhotos(Map<String, String> searchParams, String outputDirectory) throws Exception {
        // Ensure the output directory exists
        Path outputDirPath = Paths.get(outputDirectory);
        if (!Files.exists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
            System.out.println(CREATED_DIRECTORY + outputDirPath.toAbsolutePath());
        }

        StringBuilder urlBuilder = new StringBuilder(BASE_URL + SEARCH_ENDPOINT + "?");

        for (Map.Entry<String, String> entry : searchParams.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            urlBuilder.append(STR);
            urlBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            urlBuilder.append(STR2);
        }

        String urlString = urlBuilder.toString();
        urlString = urlString.substring(0, urlString.length() - 1); // Remove the trailing '&'
        System.out.println(REQUEST_URL + urlString);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(GET);

        int responseCode = connection.getResponseCode();
        System.out.println(RESPONSE_CODE + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (ZipInputStream zis = new ZipInputStream(connection.getInputStream())) {
                ZipEntry zipEntry;
                while ((zipEntry = zis.getNextEntry()) != null) {
                    Path filePath = outputDirPath.resolve(zipEntry.getName());
                    System.out.println(EXTRACTING_FILE + filePath.toAbsolutePath());
                    Files.createDirectories(filePath.getParent()); 
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                    zis.closeEntry();
                }
            }
            System.out.println(FILES_EXTRACTED_SUCCESSFULLY);
        } else {
            throw new RuntimeException(HTTP_GET_REQUEST_FAILED_WITH_RESPONSE_CODE + responseCode);
        }
    }

    public static void main(String[] args) {
        SearchClient client = new SearchClient();
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("location", "SanFrancisco");
        searchParams.put("season", "Summer");
        //searchParams.put("keyword1", "Nature");

        String outputDirectory = "incoming"; // Specify the directory to save photos

        try {
            client.searchPhotos(searchParams, outputDirectory);
            System.out.println("Photos downloaded to directory: " + outputDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

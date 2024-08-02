import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class PhotoLikeClient {
    private static final String ERROR = "Error: ";
    private static final String LIKE_COUNT = "likeCount";
    private static final String NEW_LIKE_COUNT = "New like count: ";
    private static final String MESSAGE = "message";
    private static final String SUCCESS = "Success: ";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String LIKE = "/like";
    private static final String API_PHOTOS = "/api/photos/";
    private static final String BASE_URL = "http://localhost:8000"; // Adjust this to your server's address
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void likePhoto(String photoId) throws IOException, InterruptedException {
        String url = BASE_URL + API_PHOTOS + photoId + LIKE;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode jsonNode = objectMapper.readTree(response.body());
            System.out.println(SUCCESS + jsonNode.get(MESSAGE).asText());
            System.out.println(NEW_LIKE_COUNT + jsonNode.get(LIKE_COUNT).asLong());
        } else {
            System.out.println(ERROR + response.statusCode());
            System.out.println(response.body());
        }
    }

    public static void main(String[] args) {
        try {
            likePhoto("2"); // Replace with an actual photo ID
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
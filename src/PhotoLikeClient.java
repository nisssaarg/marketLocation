import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class PhotoLikeClient {
    private static final String BASE_URL = "http://localhost:8000"; // Adjust this to your server's address
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void likePhoto(String photoId) throws IOException, InterruptedException {
        String url = BASE_URL + "/api/photos/" + photoId + "/like";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode jsonNode = objectMapper.readTree(response.body());
            System.out.println("Success: " + jsonNode.get("message").asText());
            System.out.println("New like count: " + jsonNode.get("likeCount").asLong());
        } else {
            System.out.println("Error: " + response.statusCode());
            System.out.println(response.body());
        }
    }

    public static void main(String[] args) {
        try {
            likePhoto("1"); // Replace with an actual photo ID
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
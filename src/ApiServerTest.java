import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.Map;
import java.util.HashMap;
import java.util.StringJoiner;

public class ApiServerTest {

    public static void main(String[] args) throws IOException {
        testUploadPhoto();
        testUploadMetadata();
        testLikePhoto();
        testBuyPhoto();
        testGetAllPhotos();
        testSearchPhotos();
    }

    private static void testUploadPhoto() throws IOException {
        URL url = new URL("http://localhost:8000/api/uploadPhoto");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/octet-stream");

        try (OutputStream os = connection.getOutputStream()) {
            os.write("dummy photo content".getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);

        if (responseCode == HttpURLConnection.HTTP_OK && "Uploaded file".equals(response)) {
            System.out.println("testUploadPhoto passed");
        } else {
            System.err.println("testUploadPhoto failed");
        }
    }

    private static void testUploadMetadata() throws IOException {
        URL url = new URL("http://localhost:8000/api/uploadMetadata");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = connection.getOutputStream()) {
            os.write("{\"key\": \"value\"}".getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);

        if (responseCode == HttpURLConnection.HTTP_OK && "Uploaded metadata".equals(response)) {
            System.out.println("testUploadMetadata passed");
        } else {
            System.err.println("testUploadMetadata failed");
        }
    }

    private static void testLikePhoto() throws IOException {
        URL url = new URL("http://localhost:8000/api/photos/123/like");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);

        if (responseCode == HttpURLConnection.HTTP_OK && "Like posted for photo id: 123".equals(response)) {
            System.out.println("testLikePhoto passed");
        } else {
            System.err.println("testLikePhoto failed");
        }
    }

    private static void testBuyPhoto() throws IOException {
        URL url = new URL("http://localhost:8000/api/photos/123/buy");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);

        if (responseCode == HttpURLConnection.HTTP_OK && "Photo bought with id: 123".equals(response)) {
            System.out.println("testBuyPhoto passed");
        } else {
            System.err.println("testBuyPhoto failed");
        }
    }

    private static void testGetAllPhotos() throws IOException {
        URL url = new URL("http://localhost:8000/api/photos");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);

        if (responseCode == HttpURLConnection.HTTP_OK && "List of all photos".equals(response)) {
            System.out.println("testGetAllPhotos passed");
        } else {
            System.err.println("testGetAllPhotos failed");
        }
    }

    private static void testSearchPhotos() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("keyword", "test");
        params.put("category", "nature");

        URL url = new URL("http://localhost:8000/api/photos/search?" + getParamsString(params));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);

        if (responseCode == HttpURLConnection.HTTP_OK && response.contains("test") && response.contains("nature")) {
            System.out.println("testSearchPhotos passed");
        } else {
            System.err.println("testSearchPhotos failed");
        }
    }

    private static String readResponse(HttpURLConnection connection) throws IOException {
        try (InputStream is = connection.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private static String getParamsString(Map<String, String> params) throws IOException {
        StringJoiner result = new StringJoiner("&");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }
}

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Deleteclient {

    private static final String ERROR_SETTING_UP_CONNECTION = "Error setting up connection: ";
    private static final String ERROR_READING_RESPONSE = "Error reading response: ";
    private static final String RESPONSE2 = "Response: ";
    private static final String RESPONSE_CODE = "Response Code: ";
    private static final String METHOD = "Method: ";
    private static final String URL2 = "URL: ";
    private static final String DELETE = "DELETE";
    private static final String _11226 = "11226";
    private static final String HTTP_LOCALHOST_8000_API_DELETE_PHOTOID = "http://localhost:8000/api/delete?photoid=";

    public static void main(String[] args) {
        String photoid = _11226;  // Replace with the actual photo ID
        String url = HTTP_LOCALHOST_8000_API_DELETE_PHOTOID + photoid;

        try {
            URL deleteUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) deleteUrl.openConnection();
            
            connection.setRequestMethod(DELETE);
            connection.setConnectTimeout(5000); 
            connection.setReadTimeout(5000);    

           
            System.out.println(URL2 + connection.getURL());
            System.out.println(METHOD + connection.getRequestMethod());
            
            try {
                int responseCode = connection.getResponseCode();
                System.out.println(RESPONSE_CODE + responseCode);
                
                InputStream responseStream = (responseCode == 200) ? connection.getInputStream() : connection.getErrorStream();
                
                StringBuilder response = new StringBuilder();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = responseStream.read(buffer)) != -1) {
                    response.append(new String(buffer, 0, bytesRead));
                }
                
                System.out.println(RESPONSE2 + response.toString());
            } catch (IOException e) {
                System.err.println(ERROR_READING_RESPONSE + e.getMessage());
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            System.err.println(ERROR_SETTING_UP_CONNECTION + e.getMessage());
            e.printStackTrace();
        }
    }
}
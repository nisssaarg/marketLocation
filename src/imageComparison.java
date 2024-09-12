import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class imageComparison {
    public static void main(String[] args) {
        try {
           
            File clientImage = new File("/Users/nisssaarg/Desktop/marketLocation/uploads/1724269142700_uploaded_photo.jpg");
            File serverImage = new File("/Users/nisssaarg/Desktop/9697.jpg");
            
            byte[] clientImageBytes = Files.readAllBytes(clientImage.toPath());
            byte[] serverImageBytes = Files.readAllBytes(serverImage.toPath());
            
            boolean areEqual = Arrays.equals(clientImageBytes, serverImageBytes);

            if (areEqual) {
                System.out.println("The images are identical.");
            } else {
                System.out.println("The images are different.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

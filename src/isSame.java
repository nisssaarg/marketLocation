import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class isSame {
    public static void main(String[] args) throws IOException{
        File file1 = new File("/Users/nisssaarg/Desktop/marketLocation/uploads/1724193045450_uploaded_photo.jpg");
        File file2 = new File("/Users/nisssaarg/Desktop/9697.jpg");

        String hash1 = ImageHashGenerator.generateImageHash(FileUtils.readFileToByteArray(file1));
        String hash2 = ImageHashGenerator.generateImageHash(FileUtils.readFileToByteArray(file2));

        System.out.println(hash1.equals(hash2));
    }
}

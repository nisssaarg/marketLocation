import java.nio.file.Path;

public interface FileUploaderInterface {
    boolean uploadFile(String fileName , Path filePath, byte[] fileBytes);
}

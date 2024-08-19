import java.nio.file.Path;

public interface FileUploaderInterface {
    boolean uploadFile(Path filePath, byte[] fileBytes);
}

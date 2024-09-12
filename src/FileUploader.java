import java.io.*;
import java.nio.file.Path;
import java.util.logging.Logger;

class FileUploader implements FileUploaderInterface {
    private static final String IO_EXCEPTION = "IO Exception";
    private static final String FILE_NOT_FOUND = "File not found";
    private static final String FILE_UPLOADED = "File uploaded";
    private static final Logger logger = Logger.getLogger(FileUploader.class.getName());
    //private static final String UPLOAD_DIR = "uploads";
    @Override
    public boolean uploadFile( Path filePath, byte[] fileBytes)  {
        //File uploadedFile = new File(UPLOAD_DIR + File.separator + fileName);
        try (OutputStream os = new FileOutputStream(filePath.toFile())) {
            os.write(fileBytes);
            os.flush();
            logger.info(FILE_UPLOADED);
            return true;
        }catch(FileNotFoundException e){
            logger.warning(FILE_NOT_FOUND);
        } catch (IOException e) {
           logger.warning(IO_EXCEPTION);
        }
        return false;
        
    }
}

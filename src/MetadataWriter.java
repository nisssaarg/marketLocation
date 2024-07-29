import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

public class MetadataWriter {
    private static final String METADATA_INSERTED_SUCCESSFULLY = "Metadata inserted successfully";
    private static final String FAILED_TO_INSERT_DATA = "Failed to insert data.";
    //private static final String DATA_INSERTED_SUCCESSFULLY = "Data inserted successfully.";
    private static final String NO_CONNECTION_PRESENT = "No connection present";
    private static final Logger logger = Logger.getLogger(UploadPhotoHandler.class.getName());
    public Connection connection;
    
    public MetadataWriter(){
        // todo - you will switch this to get from a connection pool
        // because, you don't want singleton and also you don't want to create connection everytime
        // when you change all that, if you get a connection in the constructor, what is the right
        // place to give it back?
        connection = DatabaseConnection.getConnection();
        if(connection == null){
            logger.info(NO_CONNECTION_PRESENT);
        }
    }

    public void writeToDatabase(Map<String, Object> metadata) {
        Metadata metadataObject = new Metadata(metadata);
        writetoDatabaseHelper(metadataObject);
    }

    public void writetoDatabaseHelper(Metadata metadata) {
        // todo - get and release connection in this method
        String sql = "INSERT INTO metadata (location,photo_path, season, subject, keyword1, keyword2, keyword3, keyword4, keyword5) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)";
    
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Assuming metadata has methods to retrieve these fields
            pstmt.setString(1, metadata.getLocation());
            pstmt.setString(2,metadata.getPhoto_path());
            pstmt.setString(3, metadata.getSeason());
            pstmt.setString(4, metadata.getSubject());
            pstmt.setString(5, metadata.getKeyword1());
            pstmt.setString(6, metadata.getKeyword2());
            pstmt.setString(7, metadata.getKeyword3());
            pstmt.setString(8, metadata.getKeyword4());
            pstmt.setString(9, metadata.getKeyword5());
    
            // Execute the insert statement
            pstmt.executeUpdate();

            // todo - commit missing and is this auto commiting? confirm -- don't auto commit.
            System.out.println(METADATA_INSERTED_SUCCESSFULLY);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(FAILED_TO_INSERT_DATA + e.getMessage());
        }
    }
}

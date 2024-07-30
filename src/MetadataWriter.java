import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

public class MetadataWriter {
    private static final String METADATA_INSERTED_SUCCESSFULLY = "Metadata inserted successfully";
    private static final String FAILED_TO_INSERT_DATA = "Failed to insert data.";
    //private static final String DATA_INSERTED_SUCCESSFULLY = "Data inserted successfully.";
    //private static final String NO_CONNECTION_PRESENT = "No connection present";
    private static final Logger logger = Logger.getLogger(MetadataWriter.class.getName());
    public Connection connection;
    
    public MetadataWriter(){
        // todo - you will switch this to get from a connection pool
        // because, you don't want singleton and also you don't want to create connection everytime
        // when you change all that, if you get a connection in the constructor, what is the right
        // place to give it back?
    }

    public void writeToDatabase(Map<String, Object> metadata) {
        Metadata metadataObject = new Metadata(metadata);
        writetoDatabaseHelper(metadataObject);
    }

    public void writetoDatabaseHelper(Metadata metadata) {
        String sql = "INSERT INTO metadata (location, photo_path, season, subject, keyword1, keyword2, keyword3, keyword4, keyword5) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabasePool.getConnection();
            // todo - move this to pool
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, metadata.getLocation());
            pstmt.setString(2, metadata.getPhoto_path());
            pstmt.setString(3, metadata.getSeason());
            pstmt.setString(4, metadata.getSubject());
            pstmt.setString(5, metadata.getKeyword1());
            pstmt.setString(6, metadata.getKeyword2());
            pstmt.setString(7, metadata.getKeyword3());
            pstmt.setString(8, metadata.getKeyword4());
            pstmt.setString(9, metadata.getKeyword5());
    
            pstmt.executeUpdate();
            conn.commit();
            //conn.rollback();
            System.out.println(METADATA_INSERTED_SUCCESSFULLY);
        } catch (SQLException e) {
            //logger.info("Here");
            if (conn != null) {
                try {
                    // todo - also do this in connection pool return connection
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            System.err.println(FAILED_TO_INSERT_DATA + e.getMessage());
        } finally {
            if (conn != null) {
                logger.info("Connection released");
                DatabasePool.releaseConnection(conn);
            }
        }
    }
}

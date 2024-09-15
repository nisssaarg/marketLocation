import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MetadataWriter {
    private static final String RESULT2 = "Result";
    private static final String METADATA_ID = "metadata_id";
    private static final String HASH_ID = "hash_id";
    private static final String INSERT_INTO_METADATA = "INSERT INTO metadata (location, season, subject, keyword1, keyword2, keyword3, keyword4, keyword5, hash_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING metadata_id";
    private static final String INSERT_INTO_HASH = "INSERT INTO hash (hash, photo_path) VALUES (?, ?) RETURNING hash_id";
    private static final String SELECT_HASH = "SELECT hash_id FROM hash where hash_id = ? FOR SHARE";
    private static final String CONNECTION_RELEASED = "Connection released";
    private static final String METADATA_INSERTED_SUCCESSFULLY = "Metadata inserted successfully";
    private static final String FAILED_TO_INSERT_DATA = "Failed to insert data.";
    private static final Logger logger = Logger.getLogger(MetadataWriter.class.getName());

    // Method to write metadata to database
    public Map<String, String> writeToDatabase(Map<String, Object> metadata) {
        Metadata metadataObject = new Metadata(metadata);
        return writeToDatabaseHelper(metadataObject);
    }

    private Map<String, String> writeToDatabaseHelper(Metadata metadata) {
        String sql = INSERT_INTO_METADATA;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, String> result = new HashMap<>();
        logger.info(metadata.toString());
        try {
            conn = DatabasePool.getConnection();
            conn.setAutoCommit(false);// Start transaction

            String lockSql = SELECT_HASH;
            PreparedStatement lockPstmt = conn.prepareStatement(lockSql);
            lockPstmt.setInt(1, metadata.getHash());  // Assuming hash_id is passed in metadata
            rs = lockPstmt.executeQuery();

            if(!rs.next()){
                conn.rollback();
                logger.severe("Hash_not found");
                throw new SQLException("hash_id not found");
            }

            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, metadata.getLocation());
            pstmt.setString(2, metadata.getSeason());
            pstmt.setString(3, metadata.getSubject());
            pstmt.setString(4, metadata.getKeyword1());
            pstmt.setString(5, metadata.getKeyword2());
            pstmt.setString(6, metadata.getKeyword3());
            pstmt.setString(7, metadata.getKeyword4());
            pstmt.setString(8, metadata.getKeyword5());
            pstmt.setInt(9, metadata.getHash());  // Assuming getHash() returns an integer

            rs = pstmt.executeQuery(); // Corrected to executeQuery
            if (rs.next()) {
                result.put(METADATA_ID, rs.getString(METADATA_ID));
            }
            conn.commit(); // Commit transaction if successful
            logger.info(METADATA_INSERTED_SUCCESSFULLY + result.toString());
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction on error
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe(FAILED_TO_INSERT_DATA + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return result;
    }

    public String savePhotoPath(String hash, String photoPath) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String hashId = null;

        try {
            conn = DatabasePool.getConnection();
            conn.setAutoCommit(false); // Start transaction
            pstmt = conn.prepareStatement(INSERT_INTO_HASH);
            pstmt.setString(1, hash);
            pstmt.setString(2, photoPath);

            rs = pstmt.executeQuery(); // Corrected to executeQuery
            if (rs.next()) {
                hashId = rs.getString(HASH_ID);
            }
            conn.commit(); // Commit transaction if successful
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction on error
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to save photo path: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return hashId;
    }


    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.severe("Failed to close ResultSet: " + e.getMessage());
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                logger.severe("Failed to close PreparedStatement: " + e.getMessage());
            }
        }
        if (conn != null) {
            try {
                conn.rollback();
                logger.info(CONNECTION_RELEASED);
                DatabasePool.releaseConnection(conn);
            } catch (SQLException e) {
                logger.severe("Failed to close Connection: " + e.getMessage());
            }
        }
    }
}

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MetadataWriter {
    private static final String RESULT2 = "Result";
    private static final String METADATA_ID = "metadata_id";
    private static final String INSERT_INTO_METADATA = "INSERT INTO metadata (location, season, subject, keyword1, keyword2, keyword3, keyword4, keyword5) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING metadata_id";
    private static final String INSERT_INTO_HASH = "INSERT INTO hash (hash, photo_path) VALUES (?, ?)";
    private static final String CONNECTION_RELEASED = "Connection released";
    private static final String METADATA_INSERTED_SUCCESSFULLY = "Metadata inserted successfully";
    private static final String FAILED_TO_INSERT_DATA = "Failed to insert data.";
    private static final Logger logger = Logger.getLogger(MetadataWriter.class.getName());

    public Map<String, String> writeToDatabase(Map<String, Object> metadata) {
        Metadata metadataObject = new Metadata(metadata);
        return writetoDatabaseHelper(metadataObject);
    }

    public Map<String, String> writetoDatabaseHelper(Metadata metadata) {
        String sql = INSERT_INTO_METADATA;
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, String> result = new HashMap<>();
        
        try {
            conn = DatabasePool.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, metadata.getLocation());
            pstmt.setString(2, metadata.getSeason());
            pstmt.setString(3, metadata.getSubject());
            pstmt.setString(4, metadata.getKeyword1());
            pstmt.setString(5, metadata.getKeyword2());
            pstmt.setString(6, metadata.getKeyword3());
            pstmt.setString(7, metadata.getKeyword4());
            pstmt.setString(8, metadata.getKeyword5());

            rs = pstmt.executeQuery();
            if (rs.next()) {
                result.put(METADATA_ID, rs.getString(METADATA_ID));
            }
            logger.info(RESULT2 + result.toString());
            conn.commit();
            System.out.println(METADATA_INSERTED_SUCCESSFULLY);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            System.err.println(FAILED_TO_INSERT_DATA + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                logger.info(CONNECTION_RELEASED);
                DatabasePool.releaseConnection(conn);
            }
        }
        return result;
    }

    public void savePhotoPath(String hash, String photoPath) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabasePool.getConnection();
            pstmt = conn.prepareStatement(INSERT_INTO_HASH);
            pstmt.setString(1, hash);
            pstmt.setString(2, photoPath);
            pstmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                DatabasePool.releaseConnection(conn);
            }
        }
    }
}

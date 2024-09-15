import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DuplicateChecker {

    private static final String SQL_ERROR_WHILE_CLOSING_RESOURCES = "SQL error while closing resources";
    private static final String SQL_ERROR_WHILE_CHECKING_FOR_DUPLICATES = "SQL error while checking for duplicates";
    private static final Logger logger = Logger.getLogger(DuplicateChecker.class.getName());
    private static final String SELECT_PHOTO_PATH_FROM_METADATA_WHERE_HASH = "SELECT * FROM HASH WHERE hash = ?";

    public static DuplicateRecord checkDuplicates(String hash) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabasePool.getConnection();
            pstmt = conn.prepareStatement(SELECT_PHOTO_PATH_FROM_METADATA_WHERE_HASH);
            pstmt.setString(1, hash);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Return a new DuplicateRecord object containing all relevant values
                String hashId = rs.getString("hash_id");
                String photoPath = rs.getString("photo_path");
                logger.info("Duplicate found: hash_id=" + hashId + ", photo_path=" + photoPath);
                return new DuplicateRecord(hashId, hash, photoPath);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, SQL_ERROR_WHILE_CHECKING_FOR_DUPLICATES, e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabasePool.releaseConnection(conn);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, SQL_ERROR_WHILE_CLOSING_RESOURCES, e);
            }
        }

        return null; // Return null if no duplicate is found
    }
}

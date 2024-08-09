import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DuplicateChecker {

    private static final Logger logger = Logger.getLogger(DuplicateChecker.class.getName());
    private static final String SELECT_PHOTO_PATH_FROM_METADATA_WHERE_HASH = "SELECT photo_path FROM metadata WHERE hash = ?";

    public static String checkDuplicates(String hash) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabasePool.getConnection();
            pstmt = conn.prepareStatement(SELECT_PHOTO_PATH_FROM_METADATA_WHERE_HASH);
            pstmt.setString(1, hash);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // If a photo path is found, log and return it
                String photoPath = rs.getString("photo_path");
                logger.info("Duplicate found for hash: " + hash + ", photo path: " + photoPath);
                return photoPath;
            } else {
                logger.info("No duplicate found for hash: " + hash);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL error while checking for duplicates", e);
        } finally {
            // Close resources in reverse order of opening them
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabasePool.releaseConnection(conn);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error while closing resources", e);
            }
        }

        return null; // Return null if no duplicate is found
    }
}

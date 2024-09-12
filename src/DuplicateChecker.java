import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DuplicateChecker {

    private static final String SQL_ERROR_WHILE_CLOSING_RESOURCES = "SQL error while closing resources";
    private static final String SQL_ERROR_WHILE_CHECKING_FOR_DUPLICATES = "SQL error while checking for duplicates";
    private static final String NO_DUPLICATE_FOUND_FOR_HASH = "No duplicate found for hash: ";
    private static final String PHOTO_PATH2 = ", photo path: ";
    private static final String DUPLICATE_FOUND_FOR_HASH = "Duplicate found for hash: ";
    private static final String PHOTO_PATH = "photo_path";
    private static final Logger logger = Logger.getLogger(DuplicateChecker.class.getName());
    private static final String SELECT_PHOTO_PATH_FROM_METADATA_WHERE_HASH = "SELECT photo_path FROM HASH WHERE hash = ?";

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
                String photoPath = rs.getString(PHOTO_PATH);
                logger.info(DUPLICATE_FOUND_FOR_HASH + hash + PHOTO_PATH2 + photoPath);
                return photoPath;
            } else {
                logger.info(NO_DUPLICATE_FOUND_FOR_HASH + hash);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, SQL_ERROR_WHILE_CHECKING_FOR_DUPLICATES, e);
        } finally {
            // Close resources in reverse order of opening them
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

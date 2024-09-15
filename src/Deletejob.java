import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Deletejob {

    private static final String FAILED_TO_DELETE_PHOTO_FILE = "Failed to delete photo file: ";
    private static final String PHOTO_FILE_DELETED_FROM = "Photo file deleted from: ";
    private static final String DELETING_PHOTO_FILE_FROM_PATH = "Deleting photo file from path: ";
    private static final String FAILED_TO_CLOSE_PREPARED_STATEMENT = "Failed to close PreparedStatement: ";
    private static final String FAILED_TO_CLOSE_RESULT_SET = "Failed to close ResultSet: ";
    private static final String FAILED_TO_ROLLBACK_TRANSACTION = "Failed to rollback transaction: ";
    private static final String FAILED_TO_EXECUTE_DELETE = "Failed to execute delete: ";
    private static final String NUMBER_OF_ROWS_DELETED = "Number of rows deleted :";
    private static final String _0_ROWS_DELETED = "0 rows deleted";
    private static final String PHOTO_PATH = "photo_path";
    public static final Logger logger = Logger.getLogger(Deletejob.class.getName());
    public static final String delete_sql = "DELETE FROM HASH h WHERE NOT EXISTS (SELECT m.hash_id FROM METADATA m WHERE h.hash_id = m.hash_id) RETURNING h.photo_path;";

    public static List<String> delete() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> photoPaths = new ArrayList<>();
        try {
            conn = DatabasePool.getConnection();
            pstmt = conn.prepareStatement(delete_sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                photoPaths.add(rs.getString(PHOTO_PATH));
            }

            if (photoPaths.isEmpty()) {
                logger.info(_0_ROWS_DELETED);
            }else{
                logger.info(NUMBER_OF_ROWS_DELETED + photoPaths.size());
            }
            conn.commit();
        } catch (SQLException e) {
            logger.severe(FAILED_TO_EXECUTE_DELETE + e.getMessage());
            rollbackConnection(conn);
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return photoPaths;
    }

    private static void rollbackConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                logger.severe(FAILED_TO_ROLLBACK_TRANSACTION + e.getMessage());
            }
        }
    }

    private static void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.severe(FAILED_TO_CLOSE_RESULT_SET + e.getMessage());
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                logger.severe(FAILED_TO_CLOSE_PREPARED_STATEMENT + e.getMessage());
            }
        }
        if (conn != null) {
            DatabasePool.releaseConnection(conn);
        }
    }

    public static void deletephoto(List<String> photoPaths) {
        for (String photo_path : photoPaths) {
            logger.info(DELETING_PHOTO_FILE_FROM_PATH + photo_path);

            try {
                Path path = Paths.get(photo_path);
                Files.deleteIfExists(path);
                logger.info(PHOTO_FILE_DELETED_FROM + photo_path);
            } catch (IOException e) {
                logger.severe(FAILED_TO_DELETE_PHOTO_FILE + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        List<String> photoPaths = delete();
        if (!photoPaths.isEmpty()) {
            deletephoto(photoPaths);
        }
    }
}

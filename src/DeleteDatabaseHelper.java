// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DeleteDatabaseHelper {

    private static final String FAILED_TO_CLOSE_CONNECTION = "Failed to close Connection: ";
    private static final String FAILED_TO_CLOSE_RESULT_SET = "Failed to close ResultSet: ";
    private static final String FAILED_TO_DELETE_PHOTO = "Failed to delete photo: ";
    private static final String FAILED_TO_CLOSE_PREPARED_STATEMENT = "Failed to close PreparedStatement: ";
    private static final String FAILED_TO_ROLLBACK_TRANSACTION = "Failed to rollback transaction: ";
    private static final String DELETE_SUCCESSFUL = "Delete successful";
    private static final String DELETED_PHOTO_HAS_HASH_ID = "Deleted photo has hash_id: ";
    private static final String PHOTO_NOT_FOUND = "Photo not found ";
    private static final String DELETING_PHOTO = "Deleting photo : ";
    // private static final String DELETE_FROM_HASH_WHERE_HASH_ID_RETURNING_HASH_ID = "DELETE FROM hash WHERE hash_id = ? RETURNING photo_path";
    // private static final String SELECT_COUNT_FROM_METADATA_WHERE_HASH_ID = "SELECT COUNT(*) FROM metadata WHERE hash_id = ?";
    private static final String HASH_ID2 = "hash_id";
    private static final String CONNECTION_RELEASED = "Connection released";
    private static final String DELETE_FROM_METADATA_WHERE_METADATA_ID_RETURNING_HASH_ID = "DELETE FROM metadata WHERE metadata_id = ? RETURNING hash_id";
    private static final Logger logger = Logger.getLogger(DeleteDatabaseHelper.class.getName());

    public static boolean deletePhoto(String photoid) {
        String sql = DELETE_FROM_METADATA_WHERE_METADATA_ID_RETURNING_HASH_ID;
        // String countSql = SELECT_COUNT_FROM_METADATA_WHERE_HASH_ID;
        // String deleteSql = DELETE_FROM_HASH_WHERE_HASH_ID_RETURNING_HASH_ID;
        Connection conn = null;
        PreparedStatement pstmt = null;
        // PreparedStatement countStmt = null;
        // PreparedStatement deleteStmt = null;
        ResultSet rs = null;
        // ResultSet countRs = null;
        // ResultSet deleteRs = null;
        logger.info(DELETING_PHOTO + photoid);

        try {
            conn = DatabasePool.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, Integer.parseInt(photoid));
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                logger.info(PHOTO_NOT_FOUND);
                return false;
            }

            int hash_id = rs.getInt(HASH_ID2);
            logger.info(DELETED_PHOTO_HAS_HASH_ID + hash_id);

            // Now check if any other rows in metadata are using this hash_id
            // countStmt = conn.prepareStatement(countSql);
            // countStmt.setInt(1, hash_id);
            // countRs = countStmt.executeQuery();

            // if (countRs.next()) {
            //     int count = countRs.getInt(1);
            //     logger.info("Number of remaining rows with hash_id " + hash_id + ": " + count);

            //     // If count is 0, we can assume no other photos use this hash_id
            //     if (count == 0) {
            //         deleteStmt = conn.prepareStatement(deleteSql);
            //         deleteStmt.setInt(1, hash_id);
            //         deleteRs = deleteStmt.executeQuery();

            //         if (deleteRs.next()) {
            //             String photo_path = deleteRs.getString("photo_path");
            //             logger.info("Deleting photo file from path: " + photo_path);

            //             // Now delete the physical file from the system
            //             try {
            //                 Path path = Paths.get(photo_path);
            //                 Files.deleteIfExists(path); // Use deleteIfExists to avoid exception if file doesn't exist
            //                 logger.info("Photo file deleted from: " + photo_path);
            //             } catch (IOException e) {
            //                 logger.severe("Failed to delete photo file: " + e.getMessage());
            //                 conn.rollback();
            //                 return false;
            //             }
            //         }
            //     }
            // }
            conn.commit();
            logger.info(DELETE_SUCCESSFUL);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction on error
                } catch (SQLException ex) {
                    logger.severe(FAILED_TO_ROLLBACK_TRANSACTION + ex.getMessage());
                }
            }
            logger.severe(FAILED_TO_DELETE_PHOTO + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return false;
    }

    private static void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        // if (deleteRs != null) {
        //     try {
        //         deleteRs.close();
        //     } catch (SQLException e) {
        //         logger.severe("Failed to close ResultSet: " + e.getMessage());
        //     }
        // }
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.severe(FAILED_TO_CLOSE_RESULT_SET + e.getMessage());
            }
        }
        // if (countRs != null) {
        //     try {
        //         countRs.close();
        //     } catch (SQLException e) {
        //         logger.severe("Failed to close ResultSet: " + e.getMessage());
        //     }
        // }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                logger.severe(FAILED_TO_CLOSE_PREPARED_STATEMENT + e.getMessage());
            }
        }
        // if (countStmt != null) {
        //     try {
        //         countStmt.close();
        //     } catch (SQLException e) {
        //         logger.severe("Failed to close PreparedStatement: " + e.getMessage());
        //     }
        // }
        // if (deleteStmt != null) {
        //     try {
        //         deleteStmt.close();
        //     } catch (SQLException e) {
        //         logger.severe("Failed to close PreparedStatement: " + e.getMessage());
        //     }
        // }
        if (conn != null) {
            try {
                conn.rollback();
                logger.info(CONNECTION_RELEASED);
                DatabasePool.releaseConnection(conn);
            } catch (SQLException e) {
                logger.severe(FAILED_TO_CLOSE_CONNECTION + e.getMessage());
            }
        }
    }
}

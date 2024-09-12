import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class duplicatePhotoRemover {
    private static final String ERROR_CLOSING_RESOURCES = "Error closing resources: ";
    private static final String ERROR_DURING_ROLLBACK = "Error during rollback: ";
    private static final String EXCEPTION_OCCURRED = "Exception occurred: ";
    private static final String DUPLICATE_RECORDS_UPDATED = "Duplicate records updated: ";
    private static final String RECORDS_UPDATED_WITH_HASH = "Records updated with hash: ";
    private static final String DUPLICATE_PHOTO_UPDATING_ENTRY_WITH_ID = "Duplicate photo, Updating entry with ID: ";
    private static final String PHOTO_PATH = "photo_path";
    private static final String METADATA_ID = "metadata_id";
    private static final String SELECT_METADATA_ID_PHOTO_PATH_FROM_METADATA = "SELECT metadata_id, photo_path, hash FROM metadata";
    private static final Logger logger = Logger.getLogger(duplicatePhotoRemover.class.getName());

    private byte[] convertFileToByteArray(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    public void removeDuplicates() {
        Connection conn = null;
        PreparedStatement selectStatement = null;
        ResultSet rs = null;

        try {
            DatabasePool.getInstance();
            conn = DatabasePool.getConnection();
           
            selectStatement = conn.prepareStatement(SELECT_METADATA_ID_PHOTO_PATH_FROM_METADATA);
            rs = selectStatement.executeQuery();

            Map<String, String> hashToPath = new HashMap<>();
            int updatedRecords = 0;
            int duplicateRecords = 0;

            while (rs.next()) {
                int id = rs.getInt(METADATA_ID);
                String photoPath = rs.getString(PHOTO_PATH);
                // Generate hash for all records
                File file = new File(photoPath);
                byte[] fileBytes = convertFileToByteArray(file);
                String hash = ImageHashGenerator.generateImageHash(fileBytes);
                if (hashToPath.containsKey(hash)) {
                    String originalPath = hashToPath.get(hash);
                    if (!originalPath.equals(photoPath)) {
                        logger.info(DUPLICATE_PHOTO_UPDATING_ENTRY_WITH_ID + id);
                        try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE metadata SET photo_path = ?, hash = ? WHERE metadata_id = ?")) {
                            updateStmt.setString(1, originalPath);
                            updateStmt.setString(2, hash);
                            updateStmt.setInt(3, id);
                            updateStmt.executeUpdate();
                            duplicateRecords++;
                        }
                    }
                } else {
                    hashToPath.put(hash, photoPath);
                    try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE metadata SET photo_path = ?, hash = ? WHERE metadata_id = ?")) {
                        updateStmt.setString(1, photoPath);
                        updateStmt.setString(2, hash);
                        updateStmt.setInt(3, id);
                        updateStmt.executeUpdate();
                        updatedRecords++;
                    }
                }
            }

            conn.commit();
            System.out.println(RECORDS_UPDATED_WITH_HASH + updatedRecords);
            System.out.println(DUPLICATE_RECORDS_UPDATED + duplicateRecords);

        } catch (SQLException | IOException e) {
            logger.severe(EXCEPTION_OCCURRED + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                logger.severe(ERROR_DURING_ROLLBACK + rollbackEx.getMessage());
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (selectStatement != null) selectStatement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                logger.severe(ERROR_CLOSING_RESOURCES + e.getMessage());
            }
        }
    }

    public void numberOfOriginals() {
        Connection conn = null;
        PreparedStatement check = null;
        Set<String> set = new HashSet<>();
        Set<String> generatedHash = new HashSet<>();

        try {
            DatabasePool.getInstance();
            conn = DatabasePool.getConnection();
            check = conn.prepareStatement("SELECT * FROM metadata");

            ResultSet rs = check.executeQuery();

            while (rs.next()) {
                set.add(rs.getString("hash"));
                File file = new File(rs.getString("photo_path"));
                //byte[] fileBytes = convertFileToByteArray(file);
                String hash = rs.getString("new_hash");
                generatedHash.add(hash);
            }
            logger.info("Set size: " + set.size());
            logger.info("Generated size: " + generatedHash.size());
            logger.info("Is same: " + generatedHash.equals(set));
        } catch (SQLException e) {
            logger.severe("SQL Error: " + e.getMessage());
        }
    }

    public void recomputeAndFixHashes() {
    Connection conn = null;
    PreparedStatement selectStatement = null;
    PreparedStatement updateStatement = null;
    ResultSet rs = null;

    try {
        DatabasePool.getInstance();
        conn = DatabasePool.getConnection();
        selectStatement = conn.prepareStatement(SELECT_METADATA_ID_PHOTO_PATH_FROM_METADATA);
        rs = selectStatement.executeQuery();

        while (rs.next()) {
            int id = rs.getInt(METADATA_ID);
            String photoPath = rs.getString(PHOTO_PATH);

            // Generate a new hash for the current photo
            File file = new File(photoPath);
            byte[] fileBytes = convertFileToByteArray(file);
            String newHash = ImageHashGenerator.generateImageHash(fileBytes);

            // Update the database with the new hash
            updateStatement = conn.prepareStatement("UPDATE metadata SET new_hash = ? WHERE metadata_id = ?");
            updateStatement.setString(1, newHash);
            updateStatement.setInt(2, id);
            updateStatement.executeUpdate();
        }
        conn.commit();
        System.out.println("All hashes updated successfully.");
    } catch (SQLException | IOException e) {
        logger.severe(EXCEPTION_OCCURRED + e.getMessage());
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException rollbackEx) {
            logger.severe(ERROR_DURING_ROLLBACK + rollbackEx.getMessage());
        }
    } finally {
        try {
            if (rs != null) rs.close();
            if (selectStatement != null) selectStatement.close();
            if (updateStatement != null) updateStatement.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.severe(ERROR_CLOSING_RESOURCES + e.getMessage());
        }
    }
}


    public static void main(String[] args) {
        duplicatePhotoRemover dpr = new duplicatePhotoRemover();
        dpr.recomputeAndFixHashes();
        dpr.numberOfOriginals();
    }
}

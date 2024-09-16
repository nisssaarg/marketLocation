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
import java.util.stream.Stream;

public class photoDeleter {
    
    private static final String STRING = " ) RETURNING hash_id;";
    private static final String DELETE_FROM_HASH_WHERE_PHOTO_PATH_NOT_IN = "DELETE FROM hash WHERE photo_path not in ( ";
    private static final Logger logger = Logger.getLogger(photoDeleter.class.getName());

    private static List<String> getPhotoList(Path Directory){
        List<String> photo_names = new ArrayList<>();
        try (Stream<Path> files = Files.list(Directory)) {
            files.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    photo_names.add(Directory.toString()+"/"+filePath.getFileName());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return photo_names;
    }

    private static void checkPhoto(List<String> files) {
        if (files.isEmpty()) {
            logger.info("No files to check.");
            return;
        }
    
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            sb.append("?");
            if (i < files.size() - 1) {
                sb.append(",");
            }
        }
    
        String sql = DELETE_FROM_HASH_WHERE_PHOTO_PATH_NOT_IN + sb.toString() + STRING;
    
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
    
        try {
            conn = DatabasePool.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);
    
            for (int i = 0; i < files.size(); i++) {
                pstmt.setString(i + 1, files.get(i));
            }
    
            rs = pstmt.executeQuery();
            while (rs.next()) {
                logger.info("Deleted Row : " + rs.getInt("hash_id"));
            }
            conn.commit();
        } catch (SQLException e) {
            logger.severe("SQL exception: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.severe("Failed to rollback connection: " + rollbackEx.getMessage());
                }
            }
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }
    
    private static void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
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
                logger.info("Connection Released");
                DatabasePool.releaseConnection(conn);
            } catch (SQLException e) {
                logger.severe("Failed to close Connection: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args){
        Path path = Paths.get("uploads");
        List<String> files = getPhotoList(path);
        logger.info("Number of files : " + files.size());
        if(files.size() != 0)
            checkPhoto(files);
    }
}

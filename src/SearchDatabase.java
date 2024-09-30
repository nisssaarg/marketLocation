import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// todo - support optional keywork searches -- don't fallback to null
public class SearchDatabase {
    private static final Logger logger = Logger.getLogger(SearchDatabase.class.getName());

    public List<String> executeSearchQuery(String sqlQuery, List<Object> parameters) {
        List<String> results = new ArrayList<>();

        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
            
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("photo_path"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Database error: " + e.getMessage());
        }

        return results;
    }
}

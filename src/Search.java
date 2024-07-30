import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Search {
    private static final Logger logger = Logger.getLogger(Search.class.getName());

    public List<String> searchPhotos(Map<String, String> queryParams) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT photo_path FROM metadata");
        List<Object> parameters = new ArrayList<>();

        // Initialize a flag to indicate if WHERE clause has been added
        boolean isFirstCondition = true;

        // Start building the WHERE clause
        if (!queryParams.isEmpty()) {
            sqlBuilder.append(" WHERE"); // Start WHERE clause

            // Check for location
            if (queryParams.containsKey("location")) {
                if (!isFirstCondition) {
                    sqlBuilder.append(" AND");
                }
                sqlBuilder.append(" location = ?");
                parameters.add(queryParams.get("location"));
                isFirstCondition = false;
            }

            // Check for season
            if (queryParams.containsKey("season")) {
                if (!isFirstCondition) {
                    sqlBuilder.append(" AND");
                }
                sqlBuilder.append(" season = ?");
                parameters.add(queryParams.get("season"));
                isFirstCondition = false;
            }

            // Check for subject
            if (queryParams.containsKey("subject")) {
                if (!isFirstCondition) {
                    sqlBuilder.append(" AND");
                }
                sqlBuilder.append(" subject = ?");
                parameters.add(queryParams.get("subject"));
                isFirstCondition = false;
            }

            // Handle keywords
            for (int i = 1; i <= 5; i++) {
                String key = "keyword" + i;
                if (queryParams.containsKey(key)) {
                    if (!isFirstCondition) {
                        sqlBuilder.append(" OR ("); // Open parenthesis for keyword conditions
                    } else {
                        sqlBuilder.append(" ("); // Open parenthesis for keyword conditions
                    }
                    String value = queryParams.get(key);
                    for (int j = 1; j <= 5; j++) {
                        sqlBuilder.append(" keyword").append(j).append(" = ? ");
                        parameters.add(value);
                        if (j < 5) {
                            sqlBuilder.append(" OR "); // Add OR between keyword checks
                        }
                    }
                    sqlBuilder.append(")"); // Close parenthesis for keyword conditions
                    isFirstCondition = false; // Ensure that this part does not need "AND" in front
                }
            }
        }
        // todo - add an else condition

        logger.info(sqlBuilder.toString());
        SearchDatabase sdb = new SearchDatabase();
        return sdb.executeSearchQuery(sqlBuilder.toString(), parameters);
    }
}

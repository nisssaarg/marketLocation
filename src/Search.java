import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Search {
    private static final String OR = " OR (";
    private static final String KEYWORD = "keyword";
    private static final String SUBJECT2 = " subject = ?";
    private static final String SUBJECT = "subject";
    private static final String SEASON2 = " season = ?";
    private static final String SEASON = "season";
    private static final String AND = " AND";
    private static final String LOCATION2 = " location = ?";
    private static final String LOCATION = "location";
    private static final String WHERE = " WHERE";
    private static final String SELECT_PHOTO_PATH_FROM_METADATA = "SELECT photo_path FROM metadata";
    private static final Logger logger = Logger.getLogger(Search.class.getName());

    public List<String> searchPhotos(Map<String, String> queryParams) {
        StringBuilder sqlBuilder = new StringBuilder(SELECT_PHOTO_PATH_FROM_METADATA);
        List<Object> parameters = new ArrayList<>();

        // Initialize a flag to indicate if WHERE clause has been added
        boolean isFirstCondition = true;

        // Start building the WHERE clause
        if (!queryParams.isEmpty()) {
            sqlBuilder.append(WHERE); // Start WHERE clause

            // Check for location
            if (queryParams.containsKey(LOCATION)) {
                if (!isFirstCondition) {
                    sqlBuilder.append(AND);
                }
                sqlBuilder.append(LOCATION2);
                parameters.add(queryParams.get(LOCATION));
                isFirstCondition = false;
            }

            // Check for season
            if (queryParams.containsKey(SEASON)) {
                if (!isFirstCondition) {
                    sqlBuilder.append(AND);
                }
                sqlBuilder.append(SEASON2);
                parameters.add(queryParams.get(SEASON));
                isFirstCondition = false;
            }

            // Check for subject
            if (queryParams.containsKey(SUBJECT)) {
                if (!isFirstCondition) {
                    sqlBuilder.append(AND);
                }
                sqlBuilder.append(SUBJECT2);
                parameters.add(queryParams.get(SUBJECT));
                isFirstCondition = false;
            }

            // Handle keywords
            for (int i = 1; i <= 5; i++) {
                String key = KEYWORD + i;
                if (queryParams.containsKey(key)) {
                    if (!isFirstCondition) {
                        sqlBuilder.append(OR); // Open parenthesis for keyword conditions
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Search {
    private static final Logger logger = Logger.getLogger(Search.class.getName());
    public List<String> searchPhotos(Map<String, String> queryParams) {

        // todo - get rid of 1=1 -- it is bad -- tell me why
        StringBuilder sqlBuilder = new StringBuilder("SELECT photo_path FROM metadata WHERE 1=1");
        // todo - bonus - think how you will support use of some ANDs and some ORs in your search - no need to implement
        List<Object> parameters = new ArrayList<>();

        if (queryParams.containsKey("location")) {
            sqlBuilder.append(" AND location = ?");
            parameters.add(queryParams.get("location"));
        }
        if (queryParams.containsKey("season")) {
            sqlBuilder.append(" AND season = ?");
            parameters.add(queryParams.get("season"));
        }
        if (queryParams.containsKey("subject")) {
            sqlBuilder.append("AND subject = ?");
            parameters.add(queryParams.get("subject"));
        }

        // Handle keywords
        for (int i = 1; i <= 5; i++) {
            String key = "keyword" + i;
            if (queryParams.containsKey(key)) {
                sqlBuilder.append(" OR (keyword1 = ? OR keyword2 = ? OR keyword3 = ? OR keyword4 = ? OR keyword5 = ?)");
                String value = queryParams.get(key);
                for (int j = 0; j < 5; j++) {
                    parameters.add(value);
                }
            }
        }
        logger.info(sqlBuilder.toString());
        SearchDatabase sdb = new SearchDatabase();
        return sdb.executeSearchQuery(sqlBuilder.toString(), parameters);
    }
}
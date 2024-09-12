import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class RedisToPostgres {
    private static final String INSERT_INTO_LIKES_METADATA_ID_LIKECOUNT_VALUES = "INSERT INTO LIKES (metadata_id, likecount) VALUES (?, ?)";
    private static final String UPDATED_LIKES_FOR_METADATA_ID = "Updated likes for metadata_id: ";
    private static final String UPDATE_LIKES_SET_LIKECOUNT_WHERE_METADATA_ID = "UPDATE LIKES SET likecount = ? WHERE metadata_id = ?";
    private static final String LIKECOUNT = "likecount";
    private static final String SELECT_LIKECOUNT_FROM_LIKES_WHERE_METADATA_ID = "SELECT likecount FROM LIKES WHERE metadata_id = ?";
    private static final String POST = "post:*";
    private static final String VALUE = ", Value: ";
    private static final String REDIS = "Redis";
    private static final String DELETED_KEY = "Deleted key:";
    private static final String PROCESSING_KEY = "Processing key: ";
    private static final String INSERTED_LIKES_FOR_METADATA_ID = "Inserted likes for metadata_id: ";
    private static final String REDIS_URI = "redis://localhost:6379"; // Correct URI format

    public static void main(String[] args) {
        try {
            DatabasePool.getInstance();
        } catch (SQLException e) {
            
            e.printStackTrace();
        } // Initialize the connection pool
        Jedis jedis = new Jedis(REDIS_URI); // Assume Redis connection is handled here
   
        try {
            transferLikesToPostgres(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close(); // Close Redis connection if needed
        }
    }
    

    private static void transferLikesToPostgres(Jedis jedis) throws NumberFormatException, SQLException {
        Set<String> keys = getLikesKeys(jedis);
        for (String key : keys) {
            String likesValue = jedis.get(key);
            String[] parts = key.split(":");
            int metadataId = Integer.parseInt(parts[1]); // Extracting metadata ID from the key
    
            System.out.println(PROCESSING_KEY + key + VALUE + likesValue);
            updateLikesInPostgres(metadataId, Integer.parseInt(likesValue));

            jedis.del(key);
            System.out.println(DELETED_KEY + key + REDIS);
        }
    }
    

    private static Set<String> getLikesKeys(Jedis jedis) {
        return jedis.keys(POST); 
    }

    private static void updateLikesInPostgres(int metadataId, int likesValue) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabasePool.getConnection(); 

            // Check if the record exists
            String selectQuery = SELECT_LIKECOUNT_FROM_LIKES_WHERE_METADATA_ID;
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setInt(1, metadataId);
                ResultSet resultSet = selectStatement.executeQuery();

                if (resultSet.next()) {
                    // Update the existing record
                    int currentLikes = resultSet.getInt(LIKECOUNT);
                    int newLikes = currentLikes + likesValue;
                    String updateQuery = UPDATE_LIKES_SET_LIKECOUNT_WHERE_METADATA_ID;
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        updateStatement.setInt(1, newLikes);
                        updateStatement.setInt(2, metadataId);
                        updateStatement.executeUpdate();
                        connection.commit();
                        System.out.println(UPDATED_LIKES_FOR_METADATA_ID + metadataId);
                    }
                } else {
                    // Insert a new record if it doesn't exist
                    String insertQuery = INSERT_INTO_LIKES_METADATA_ID_LIKECOUNT_VALUES;
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                        insertStatement.setInt(1, metadataId);
                        insertStatement.setInt(2, likesValue);
                        insertStatement.executeUpdate();
                        connection.commit();
                        System.out.println(INSERTED_LIKES_FOR_METADATA_ID + metadataId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.commit();
            if (connection != null) {
                DatabasePool.releaseConnection(connection); // Release the connection back to the pool
            }
        }
    }
}

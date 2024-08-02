import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class RedisToPostgres {
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
    
            System.out.println("Processing key: " + key + ", Value: " + likesValue);
            updateLikesInPostgres(metadataId, Integer.parseInt(likesValue));

            jedis.del(key);
            System.out.println("Deleted key:" + key + "Redis");
        }
    }
    

    private static Set<String> getLikesKeys(Jedis jedis) {
        return jedis.keys("post:*"); 
    }

    private static void updateLikesInPostgres(int metadataId, int likesValue) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabasePool.getConnection(); 

            // Check if the record exists
            String selectQuery = "SELECT likecount FROM LIKES WHERE metadata_id = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setInt(1, metadataId);
                ResultSet resultSet = selectStatement.executeQuery();

                if (resultSet.next()) {
                    // Update the existing record
                    int currentLikes = resultSet.getInt("likecount");
                    int newLikes = currentLikes + likesValue;
                    String updateQuery = "UPDATE LIKES SET likecount = ? WHERE metadata_id = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        updateStatement.setInt(1, newLikes);
                        updateStatement.setInt(2, metadataId);
                        updateStatement.executeUpdate();
                        connection.commit();
                        System.out.println("Updated likes for metadata_id: " + metadataId);
                    }
                } else {
                    // Insert a new record if it doesn't exist
                    String insertQuery = "INSERT INTO LIKES (metadata_id, likecount) VALUES (?, ?)";
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                        insertStatement.setInt(1, metadataId);
                        insertStatement.setInt(2, likesValue);
                        insertStatement.executeUpdate();
                        connection.commit();
                        System.out.println("Inserted likes for metadata_id: " + metadataId);
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

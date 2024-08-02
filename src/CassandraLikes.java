import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Row;

public class CassandraLikes {
    private static final String POST_PREFIX = "post:";
    private static CqlSession session;

    static {
        // Initialize Cassandra session
        session = CqlSession.builder()
                .withKeyspace("your_keyspace_name") // Replace with your keyspace
                .build();
    }

    public static void likePost(String postId) {
        // Increment the like count for the post
        SimpleStatement statement = SimpleStatement.builder(
                "UPDATE likes SET like_count = like_count + 1 WHERE post_id = ?")
                .addPositionalValue(POST_PREFIX + postId)
                .build();

        session.execute(statement);
        System.out.println("Liked post " + POST_PREFIX + postId);
    }

    // Function to get the like count for a post
    public static long getLikes(String postId) {
        SimpleStatement statement = SimpleStatement.builder(
                "SELECT like_count FROM likes WHERE post_id = ?")
                .addPositionalValue(POST_PREFIX + postId)
                .build();

        Row row = session.execute(statement).one();
        return row != null ? row.getLong("like_count") : 0;
    }

    public static void close() {
        if (session != null) {
            session.close();
        }
    }

    public static void main(String[] args) {
        String testPostId = "12345";
        try {
            // Like the post multiple times
            for (int i = 0; i < 5; i++) {
                likePost(testPostId);
            }

            // Get and print the like count
            long likeCount = getLikes(testPostId);
            System.out.println("Like count for post " + testPostId + ": " + likeCount);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the Cassandra session
            close();
        }
    }
}

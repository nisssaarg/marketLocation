import redis.clients.jedis.Jedis;

public class RedisLikes {
    private static final String LIKES = ":likes";
    private static final String POST = "post:";
    private static Jedis jedis = new Jedis("localhost", 6379);

    public static void likePost(String postId) {
        // todo - understand thread safety
        jedis.incr(POST + postId + LIKES);
        System.out.println("Liked post " + postId);
    }

    // Function to get the like count for a post
    public static long getLikes(String postId) {
        String likes = jedis.get(POST + postId + LIKES);
        return likes == null ? 0 : Long.parseLong(likes);
    }
}

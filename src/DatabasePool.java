import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class DatabasePool {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/photomarketplace";
    private static final String USER = "nisssaarg";
    private static final String PASS = "icecubes";
    private static final int INITIAL_POOL_SIZE = 10;

    private static final Queue<Connection> connectionPool = new ConcurrentLinkedQueue<>();
    private static final Logger logger = Logger.getLogger(DatabasePool.class.getName());

    private static DatabasePool instance;

    private DatabasePool() {
        // Private constructor
    }

    public static synchronized DatabasePool getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabasePool();
            create(); // Initialize the connection pool
        }
        return instance;
    }

    private static void create() throws SQLException {
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            connectionPool.add(createConnection());
        }
    }

    private static Connection createConnection() throws SQLException {
        logger.info("Creating new connection");
        Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
        connection.setAutoCommit(false); // Disable auto-commit
        return connection;
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = connectionPool.poll();
        if (connection == null) {
            if (connectionPool.size() < INITIAL_POOL_SIZE) {
                connection = createConnection();
            } else {
                throw new SQLException("Maximum pool size reached, no available connections!");
            }
        }
        logger.info("Obtained connection: " + connection);
        logger.info("Connections left: " + connectionPool.size());
        return connection;
    }

    public static void releaseConnection(Connection connection) {
        try {
            if (!connection.isClosed()) {
                connection.rollback();
                connectionPool.offer(connection); 
                logger.info("Connection released");
            } else {
                logger.severe("Connection is closed and cannot be returned to the pool.");
            }
        } catch (SQLException e) {
            logger.severe("Failed to release connection: " + e.getMessage());
        }
    }

    public static void closeAllConnections() {
        for (Connection connection : connectionPool) {
            try {
                connection.close();
                logger.info("Closed connection: " + connection);
            } catch (SQLException e) {
                logger.severe("Failed to close connection: " + e.getMessage());
            }
        }
    }
}

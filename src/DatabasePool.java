import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class DatabasePool {
    private static final String FAILED_TO_CLOSE_CONNECTION = "Failed to close connection: ";
    private static final String CLOSED_CONNECTION = "Closed connection: ";
    private static final String FAILED_TO_RELEASE_CONNECTION = "Failed to release connection: ";
    private static final String CONNECTION_IS_CLOSED_AND_CANNOT_BE_RETURNED_TO_THE_POOL = "Connection is closed and cannot be returned to the pool.";
    private static final String CONNECTION_RELEASED = "Connection released";
    private static final String CONNECTIONS_LEFT = "Connections left: ";
    private static final String OBTAINED_CONNECTION = "Obtained connection: ";
    private static final String MAXIMUM_POOL_SIZE_REACHED_NO_AVAILABLE_CONNECTIONS = "Maximum pool size reached, no available connections!";
    private static final String CREATING_NEW_CONNECTION = "Creating new connection";
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
        logger.info(CREATING_NEW_CONNECTION);
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
                throw new SQLException(MAXIMUM_POOL_SIZE_REACHED_NO_AVAILABLE_CONNECTIONS);
            }
        }
        logger.info(OBTAINED_CONNECTION + connection);
        logger.info(CONNECTIONS_LEFT + connectionPool.size());
        return connection;
    }

    public static void releaseConnection(Connection connection) {
        try {
            if (!connection.isClosed()) {
                connection.rollback();
                connectionPool.offer(connection); 
                logger.info(CONNECTION_RELEASED);
            } else {
                logger.severe(CONNECTION_IS_CLOSED_AND_CANNOT_BE_RETURNED_TO_THE_POOL);
            }
        } catch (SQLException e) {
            logger.severe(FAILED_TO_RELEASE_CONNECTION + e.getMessage());
        }
    }

    public static void closeAllConnections() {
        for (Connection connection : connectionPool) {
            try {
                connection.close();
                logger.info(CLOSED_CONNECTION + connection);
            } catch (SQLException e) {
                logger.severe(FAILED_TO_CLOSE_CONNECTION + e.getMessage());
            }
        }
    }
}

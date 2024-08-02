import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DatabasePool {
    private static final String CONNECTION_NOT_GOOD = "Connection not good";
    private static final String CONNECTION_RELEASED = "Connection released";
    private static final String CONNECTION_OBTAINED_FROM_POOL = "Connection obtained from pool";
    private static final String MAXIMUM_POOL_SIZE_REACHED_NO_AVAILABLE_CONNECTIONS = "Maximum pool size reached, no available connections!";
    private static final String CONNECTION_CREATED = "Connection created";
    private static final String CONNECTION_POOL_CREATED = "Connection pool created";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/photomarketplace";
    private static final String USER = "nisssaarg";
    private static final String PASS = "icecubes";
    private static final int INITIAL_POOL_SIZE = 10;

    private static List<Connection> connectionPool;
    private static List<Connection> usedConnections = new ArrayList<>();
    private static DatabasePool instance;

    private static final Logger logger = Logger.getLogger(DatabasePool.class.getName());
    
    private DatabasePool() {
        // private constructor
    }

    public static synchronized DatabasePool getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabasePool();
            create(); // Call create when the instance is initialized
        }
        return instance;
    }

    public static void create() throws SQLException {
        logger.info("Creating connection pool");
        connectionPool = new ArrayList<>(INITIAL_POOL_SIZE);
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            connectionPool.add(createConnection());
        }
    }
    
    // Method to create a new connection
    private static Connection createConnection() throws SQLException {
        logger.info(CONNECTION_CREATED);
        Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
        connection.setAutoCommit(false); // Disable auto-commit
        return connection;
    }

    // Method to get a connection from the pool
    public synchronized static Connection getConnection() throws SQLException {
        if (connectionPool.isEmpty()) {
            if (usedConnections.size() < INITIAL_POOL_SIZE) {
                connectionPool.add(createConnection());
            } else {
                throw new SQLException(MAXIMUM_POOL_SIZE_REACHED_NO_AVAILABLE_CONNECTIONS);
            }
        }
        Connection connection = connectionPool.remove(connectionPool.size() - 1);
        usedConnections.add(connection);
        logger.info(CONNECTION_OBTAINED_FROM_POOL);
        return connection;
    }
    
    public synchronized static void releaseConnection(Connection connection) {
        logger.info(CONNECTION_RELEASED);
        try {
            // Check if the connection is still valid before returning it to the pool
            if (!connection.isClosed()) {
                connection.rollback(); // Roll back any changes
                connectionPool.add(connection);
                usedConnections.remove(connection);
            } else {
                logger.severe("Connection is closed and cannot be returned to the pool.");
            }
        } catch (SQLException e) {
            logger.severe(CONNECTION_NOT_GOOD);
            e.printStackTrace();
        }
    }
    

    // Example usage
    public static void main(String[] args) {
        try {
            create();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Connection conn = null;
        try {
            conn = getConnection();
            logger.info("Obtained connection: " + conn);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                releaseConnection(conn); // Ensure to release the connection
            }
        }
    }
}

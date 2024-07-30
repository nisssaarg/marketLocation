import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DatabasePool {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/photomarketplace";
    private static final String USER = "nisssaarg";
    private static final String PASS = "icecubes";
    private static final int INITIAL_POOL_SIZE = 10;

    private static List<Connection> connectionPool;
    private static List<Connection> usedConnections = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(DatabasePool.class.getName());
    
    // Method to initialize the pool
    // todo - make the connection pool singleton
    public static void create() throws SQLException {
        logger.info("Connection pool created");
        connectionPool = new ArrayList<>(INITIAL_POOL_SIZE); // Initialize the connectionPool
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            connectionPool.add(createConnection());
        }
    }

    // Method to create a new connection
    private static Connection createConnection() throws SQLException {
        logger.info("Connection created");
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // Method to get a connection from the pool
    public synchronized static Connection getConnection() throws SQLException {
        if (connectionPool.isEmpty()) {
            if (usedConnections.size() < INITIAL_POOL_SIZE) {
                connectionPool.add(createConnection());
            } else {
                throw new SQLException("Maximum pool size reached, no available connections!");
            }
        }
        Connection connection = connectionPool.remove(connectionPool.size() - 1);
        // todo - think what should happen if you encounter an error here
        usedConnections.add(connection);
        logger.info("Connection obtained from pool");
        return connection;
    }
    
    public synchronized static void releaseConnection(Connection connection) {
        logger.info("Connection released");
        connectionPool.add(connection);
        // todo - think what should happen if you encounter an error here
        usedConnections.remove(connection);
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

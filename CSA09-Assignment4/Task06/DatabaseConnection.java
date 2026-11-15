package Task06;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection - Manages JDBC connections to MySQL database.
 * Supports configurable connection parameters via system properties or environment variables,
 * with fallback defaults.
 */
public class DatabaseConnection {

    // Default connection parameters (Overridable via System properties or environment)
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "campus_events";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private static String host = System.getProperty("db.host", System.getenv().getOrDefault("DB_HOST", DEFAULT_HOST));
    private static String port = System.getProperty("db.port", System.getenv().getOrDefault("DB_PORT", DEFAULT_PORT));
    private static String dbName = System.getProperty("db.name", System.getenv().getOrDefault("DB_NAME", DEFAULT_DB));
    private static String username = System.getProperty("db.user", System.getenv().getOrDefault("DB_USER", DEFAULT_USER));
    private static String password = System.getProperty("db.pass", System.getenv().getOrDefault("DB_PASS", DEFAULT_PASS));

    static {
        // Attempt to load MySQL JDBC Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Driver may also be loaded automatically via SPI in modern JDBC
            System.err.println("[DatabaseConnection] MySQL Driver not found on classpath: " + e.getMessage());
        }
    }

    public static String getJdbcUrl() {
        return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=3000",
                host, port, dbName);
    }

    /**
     * Obtains a new JDBC Connection to MySQL.
     * @return java.sql.Connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getJdbcUrl(), username, password);
    }

    /**
     * Checks whether MySQL database is actively reachable.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static void setCredentials(String newHost, String newPort, String newDb, String newUser, String newPass) {
        host = newHost;
        port = newPort;
        dbName = newDb;
        username = newUser;
        password = newPass;
    }

    public static String getUsername() {
        return username;
    }

    public static String getDbName() {
        return dbName;
    }
}

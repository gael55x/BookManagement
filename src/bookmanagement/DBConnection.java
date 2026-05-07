package bookmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String MYSQL_PASSWORD = "";

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "bookmanagement";
    private static final String USERNAME = "root";

    /**
     * JDBC URL for MySQL 8+ (Connector/J 8.x uses this driver class).
     * useSSL=false and serverTimezone help avoid common connection warnings on local XAMPP.
     */
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";


    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found. Add mysql-connector-j to your project libraries.");
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, MYSQL_PASSWORD);
    }
}

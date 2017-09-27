package sg.edu.nus.cs3205.subsystem3.dao;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DB {
    private static DataSource datasource = null;
    private static Context initContext = null;
    private static Connection conn = null;
    private static String connectURL = "";

    public static void setConfiguration(String connectionURL) {
        connectURL = connectionURL;
    }

    public static DataSource datasource() {
        if (datasource != null) {
            return datasource;
        }
        establishConnection();
        return datasource;
    }

    public static void establishConnection() {
        if (datasource == null) {
            try {
                initContext = new InitialContext();
                // connection = "java:comp/env/jdbc/TestDB"
                datasource = (DataSource) initContext.lookup(connectURL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (conn != null) {
            return conn;
        }
        return (conn = datasource().getConnection());
    }

}

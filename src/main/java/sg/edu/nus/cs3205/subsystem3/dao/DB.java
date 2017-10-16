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
    private static String connectURL = "java:comp/env/jdbc/TestDB";

    public static void setConfiguration(final String connectionURL) {
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
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
  	//
  	// Database Connection and Execution
  	//
  	////////////////////////////////////////////////////////////////////////

  	// Get the connection from tomcat and return the connection to the caller
    public static Connection connectDatabase(){
      if (conn != null){
        return conn;
      }
  		try{
  			conn = datasource().getConnection();
  		}catch(Exception e){
  			e.printStackTrace();
  		}
      return conn;
    }

}

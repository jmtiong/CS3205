package sg.edu.nus.cs3205.subsystem3.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DBQueryParser {

    /**
     * Query the database table
     *
     * @param table table name to query from
     * @param columns to be retrieved
     * @param conditions to be applied
     * @param variables to be inserted
     * @return Result of the query
     */
    public static ResultSet query(final String table, final String[] columns, final String[] conditions,
            final Object[] variables) throws SQLException, Exception {
        return query(table, columns, conditions, variables, null);
    }

    /**
     * Query the database table
     *
     * @param table table name to query from
     * @param columns to be retrieved
     * @param conditions to be applied
     * @param variables to be inserted
     * @param orderby to be arranged
     * @return Result of the query
     */
    public static ResultSet query(final String table, final String[] columns, final String[] conditions,
            final Object[] variables, final String[] orderby) throws SQLException, Exception {

        String query = "SELECT ";
        // columns to retrieve
        if (columns != null && columns.length > 0) {
            for (final String column : columns) {
                query += column + ", ";
            }
            query += "''";
        } else {
            query += "* ";
        }
        query += "FROM CS3205." + table + " ";
        // conditions to apply
        if (conditions != null && conditions.length > 0 && variables != null && variables.length > 0
                && variables.length == conditions.length) {
            query += "WHERE 1=1 ";
            for (final String condition : conditions) {
                // condition = (something = ?)
                query += "AND " + condition;
            }
        }
        // order by to apply
        if (orderby != null && orderby.length > 0) {
            query += "ORDER BY '' ";
            for (final String order : orderby) {
                query += ", " + order;
            }
        }
        query += ";";
        PreparedStatement ps = DB.connectDatabase().prepareStatement(query);
        if (conditions != null && conditions.length > 0 && variables != null && variables.length > 0
                && variables.length == conditions.length) {
            int i = 0;
            for (final String condition : conditions) {
                ps = updateVariables(ps, variables[i], i);
                i++;
            }
        }
        final ResultSet rs = ps.executeQuery();
        return rs;
    }

    /**
     * Insert a user record into server 3
     *
     * @param Object[] values, all the values for the user
     * @return int value indicating how many rows are affected, 0 if insert
     *         fail.
     */
    public static int insertUser(final Object[] values) {
        final String sql = "INSERT INTO CS3205.user VALUES (?, ?, ?);";
        int result = 0;
        try {
            PreparedStatement ps = DB.connectDatabase().prepareStatement(sql);
            int i = 0;
            for (final Object value : values) {
                ps = updateVariables(ps, values[i], i);
                i++;
            }
            result = ps.executeUpdate();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Insert the variables into the placeholder of the prepareStatement
     *
     * @param ps the PreparedStatement to use
     * @param argObj the object to be placed
     * @param pt the position to be placed
     * @return PreparedStatement that was updated
     */
    private static PreparedStatement updateVariables(final PreparedStatement ps, final Object argObj,
            final int pt) throws SQLException, Exception {
        if (argObj == null) {
            ps.setNull(pt + 1, 0);
        } else if (String.class.isInstance(argObj)) {
            ps.setString(pt + 1, (String) argObj);
        } else if (Integer.class.isInstance(argObj)) {
            ps.setInt(pt + 1, (Integer) argObj);
        } else if (Long.class.isInstance(argObj)) {
            ps.setLong(pt + 1, (Long) argObj);
        } else if (Double.class.isInstance(argObj)) {
            ps.setDouble(pt + 1, (Double) argObj);
        } else if (Float.class.isInstance(argObj)) {
            ps.setFloat(pt + 1, (Float) argObj);
        } else if (Date.class.isInstance(argObj)) {
            final java.sql.Date sqlDate = new java.sql.Date(((Date) argObj).getTime());
            ps.setDate(pt + 1, sqlDate);
        } else if (argObj instanceof byte[]) {
            ps.setBytes(pt + 1, (byte[]) argObj);
        } else if (java.sql.Blob.class.isInstance(argObj)) {
            ps.setBlob(pt + 1, (java.sql.Blob) argObj);
        } else {
            final String argClassName = argObj.getClass().getName();
            throw new Exception("Unknown argument type (" + pt + ") : " + (argClassName));
        }
        return ps;
    }

}

package com.cs3205.db.core;

import java.util.Date;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBQueryParser{

  public static ResultSet query(String table, String[] columns, String[] conditions, Object[] variables) throws SQLException, Exception{
      return query(table, columns, conditions, variables, null);
  }

  public static ResultSet query(String table, String[] columns, String[] conditions, Object[] variables, String[] orderby) throws SQLException, Exception{

    String query = "SELECT ";
    PreparedStatement ps = DB.getConnection().prepareStatement(query);
    // columns to retrieve
    if ( columns != null && columns.length > 0 ){
      for ( String column : columns ){
        query += column + ", ";
      }
      // remove the last ,
      query = new StringBuilder(query).replace(query.lastIndexOf(","), query.lastIndexOf(",") + 1, "").toString();
    } else {
      query += "* ";
    }
    query += "FROM " + table + " ";
    // conditions to apply
    if ( conditions != null && conditions.length > 0  && variables != null && variables.length > 0 && variables.length == conditions.length){
       query += "WHERE ";
       int i = 0;
       for ( String condition : conditions ){
         query += condition + ", ";
         ps = updateVariables(ps, variables[i], i);
         i++;
       }
       ps.executeUpdate();
       // remove the last ,
       query = new StringBuilder(query).replace(query.lastIndexOf(","), query.lastIndexOf(",") + 1, "").toString();
    }
    // order by to apply
    if ( orderby != null && orderby.length > 0 ){
      query += "ORDER BY ";
      for ( String order : orderby ){
        query += order + ", ";
        // remove the last ,
        query = new StringBuilder(query).replace(query.lastIndexOf(","), query.lastIndexOf(",") + 1, "").toString();
      }
    }
    query += ";";
    ResultSet rs = ps.executeQuery(query);
    while(rs.next()){
      System.out.println("foo: "+rs.getString("foo")+" bar: "+rs.getString("bar"));
    }
    return rs;
  }

  private static PreparedStatement updateVariables(PreparedStatement ps, Object argObj, int pt) throws SQLException, Exception{
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
			java.sql.Date sqlDate = new java.sql.Date(((Date) argObj).getTime());
			ps.setDate(pt + 1, sqlDate);
		} else if(argObj instanceof byte[]) {
			ps.setBytes(pt + 1, (byte[]) argObj);
		} else if (java.sql.Blob.class.isInstance(argObj)) {
			ps.setBlob(pt + 1, (java.sql.Blob) argObj);
		} else {
			String argClassName = argObj.getClass().getName();
			throw new Exception("Unknown argument type (" + pt + ") : " + (argClassName));
		}
    return ps;
  }

}

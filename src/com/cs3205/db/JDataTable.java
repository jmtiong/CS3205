package com.cs3205.db;

import java.util.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.cs3205.db.core.DBQueryParser;

public class JDataTable{
  String tableName = "";
  Map<String, JDataObject> table = null;
  ResultSet rs = null;
  public JDataTable(String tableName){
    this.tableName = tableName;
  }

  public JDataObject getDataObject(String oid){
    if(table == null){
      getAllObjects();
    }

    return table.get(oid);
  }

  public List<JDataObject> getAllObjects(){
    if(table != null){
      List<JDataObject> list = new ArrayList<JDataObject>(table.values());
      return list;
    }
    return queryAll();
  }

  private List<JDataObject> queryAll(){
    return query(null, null, null);
  }

  public List<JDataObject> query(String[] columns, String[] conditions, Object[] variables){
    List<JDataObject> rows = new ArrayList<>();
    table = new HashMap<>();
    try{
      rs = DBQueryParser.query(tableName, columns, conditions, variables);
      while(rs.next()){
        JDataObject row = new JDataObject();
        for (String column : getColumns()) {
          row.put(column, rs.getObject(column));
        }
        table.put(rs.getString("oid"), row);
        rows.add(row);
      }
    } catch(Exception s){
      s.printStackTrace();
    }
    return rows;
  }

  public List<String> getColumns(){
    List<String> columns = new ArrayList<>();
    try{
      ResultSetMetaData rsmd = rs.getMetaData();
      for(int i = 0; i < rsmd.getColumnCount(); i++){
        columns.add(rsmd.getColumnName(i));
      }
    }catch(Exception s){
      s.printStackTrace();
    }
    return columns;
  }
}

package sg.edu.nus.cs3205.subsystem3.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDataTable {
    String tableName = "";
    Map<String, JDataObject> table = null;
    ResultSet rs = null;

    public JDataTable(final String tableName) {
        this.tableName = tableName;
    }

    public JDataObject getDataObject(final String oid) {
        if (this.table == null) {
            this.getAllObjects();
        }

        return this.table.get(oid);
    }

    public List<JDataObject> getAllObjects() {
        if (this.table != null) {
            final List<JDataObject> list = new ArrayList<JDataObject>(this.table.values());
            return list;
        }
        return this.queryAll();
    }

    private List<JDataObject> queryAll() {
        return this.query(null, null, null);
    }

    public List<JDataObject> query(final String[] columns, final String[] conditions,
            final Object[] variables) {
        final List<JDataObject> rows = new ArrayList<>();
        this.table = new HashMap<>();
        try {
            this.rs = DBQueryParser.query(this.tableName, columns, conditions, variables);
            while (this.rs.next()) {
                final JDataObject row = new JDataObject();
                for (final String column : this.getColumns()) {
                    row.put(column, this.rs.getObject(column));
                }
                this.table.put(this.rs.getString("oid"), row);
                rows.add(row);
            }
        } catch (final Exception s) {
            s.printStackTrace();
        }
        return rows;
    }

    public List<String> getColumns() {
        final List<String> columns = new ArrayList<>();
        try {
            final ResultSetMetaData rsmd = this.rs.getMetaData();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                columns.add(rsmd.getColumnName(i));
            }
        } catch (final Exception s) {
            s.printStackTrace();
        }
        return columns;
    }
}

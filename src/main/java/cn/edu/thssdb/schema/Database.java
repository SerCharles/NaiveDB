package cn.edu.thssdb.schema;

import cn.edu.thssdb.exception.DuplicateTableException;
import cn.edu.thssdb.exception.FileIOException;
import cn.edu.thssdb.exception.TableNotExistException;
import cn.edu.thssdb.query.*;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.query.QueryResult;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.edu.thssdb.utils.Global.DATA_DIRECTORY;

public class Database {
    
    private String name;
    private HashMap<String, Table> tables;
    ReentrantReadWriteLock lock;
    
    public Database(String name) {
        this.name = name;
        this.tables = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
        recover();
    }
    
    private void persist() {
        for (Table table : tables.values()) {
            String filename = DATA_DIRECTORY + "meta_" + name + "_" + table.tableName + ".data";
            ArrayList<Column> columns = table.columns;
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                OutputStreamWriter writer = new OutputStreamWriter(fos);
                for (Column column : columns) {
                    writer.write(column.toString() + "\n");
                }
                writer.close();
                fos.close();
            } catch (Exception e) {
                throw new FileIOException(filename);
            }
        }
    }
    
    public void create(String name, Column[] columns) {
        try {
            lock.writeLock().lock();
            if (tables.containsKey(name))
                throw new DuplicateTableException(name);
    
            Table newTable = new Table(this.name, name, columns);
            tables.put(name, newTable);
            persist();
            //newTable.persist();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public Table get(String name) {
        try {
            lock.readLock().lock();
            if (!tables.containsKey(name))
                throw new TableNotExistException(name);
            return tables.get(name);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void drop(String name) {
        try {
            lock.writeLock().lock();
            if (!tables.containsKey(name))
                throw new TableNotExistException(name);
            String metaFilename = DATA_DIRECTORY + "meta_" + this.name + "_" + name + ".data";
            File metaFile = new File(metaFilename);
            if (metaFile.isFile())
                metaFile.delete();
            Table table = tables.get(name);
            table.dropSelf();
            tables.remove(name);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void dropSelf() {
        try {
            lock.writeLock().lock();
            final String filenamePrefix = DATA_DIRECTORY + "meta_" + this.name + "_";
            final String filenameSuffix = ".data";
            for (Table table : tables.values()) {
                File metaFile = new File(filenamePrefix + table.tableName + filenameSuffix);
                if (metaFile.isFile())
                    metaFile.delete();
                table.dropSelf();
//                tables.remove(table.tableName);
            }
            tables.clear();
            tables = null;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public QueryResult select(String[] columnsProjected, QueryTable the_table, Logic select_logic, boolean distinct) {
        try {
            String result_string = "";
            lock.readLock().lock();
            the_table.SetLogicSelect(select_logic);
            QueryResult query_result = new QueryResult(the_table, columnsProjected, distinct);
            query_result.GenerateQueryRecords();
            return query_result;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private void recover() {
        File dir = new File(DATA_DIRECTORY);
        File[] fileList = dir.listFiles();
        if (fileList == null)
            return;
        
        final String meta = "meta";
        for (File f : fileList) {
            if (!f.isFile())
                continue;
            try {
                String[] parts = f.getName().split("\\.")[0].split("_");
                if (!parts[0].equals(meta))
                    continue;
                if (!parts[1].equals(this.name))
                    continue;
                String tableName = parts[2];
                if (tables.containsKey(tableName))
                    throw new DuplicateTableException(tableName);
    
                ArrayList<Column> columns = new ArrayList<>();
                InputStreamReader reader = new InputStreamReader(new FileInputStream(f));
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] info = line.split(",");
                    String columnName = info[0];
                    ColumnType columnType = ColumnType.valueOf(info[1]);
                    int primaryKey = Integer.parseInt(info[2]);
                    boolean notNull = Boolean.parseBoolean(info[3]);
                    int maxLen = Integer.parseInt(info[4]);
                    Column column = new Column(columnName, columnType, primaryKey, notNull, maxLen);
                    columns.add(column);
                }
                Table table = new Table(this.name, tableName, columns.toArray(new Column[0]));
                tables.put(tableName, table);
                bufferedReader.close();
                reader.close();
            } catch (Exception e) {
                continue;
            }
        }
    }
    
    public void quit() {
        try {
            lock.writeLock().lock();
            for (Table table : tables.values()) {
                table.persist();
            }
            persist();
        } catch (Exception e) {
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String get_name()
    {
        return name;
    }

    /**
     * 描述：显示数据库单一表信息
     * 参数：无
     * 返回：string，单一表信息
     */
    public String ShowOneTable(String tableName) {
        Table table = get(tableName);
        return table.ToString();
    }
    
    /**
     * 描述：显示整个数据库信息
     * 参数：无
     * 返回：string，数据库信息
     */
    public String ToString() {
        // 迭代值
        String Top = "Database Name: " + name;
        String result = Top + "\n" + "\n";
        if(tables.isEmpty()) {
            return "Empty database!";
        }
        for (Table the_table : tables.values()) {
            if(the_table == null) {
                continue;
            }
            result += the_table.ToString();
        }
        return result;
    }
    
    /**
     * 描述：处理插入元素
     * 参数：table name，待插入列名，待插入的值（string）
     * 返回：无
     */
    public void insert(String table_name, String[] column_names, String[] values) {
        Table the_table = get(table_name);
        if(column_names == null)
        {
            the_table.insert(values);
        }
        else
        {
            the_table.insert(column_names, values);
        }
    }

    
    /**
     * 描述：处理删除元素
     * 参数：table name，删除逻辑
     * 返回：描述性语句
     */
    public String delete(String table_name, Logic the_logic) {
        Table the_table = get(table_name);
        return the_table.delete(the_logic);
    }


    /**
     * 描述：处理更新元素
     * 参数：table name，待更新的单一列名，待更新的值，符合条件
     * 返回：描述性语句
     */
    public String update(String table_name, String column_name, Comparer value, Logic the_logic) {
        Table the_table = get(table_name);
        return the_table.update(column_name, value, the_logic);
    }

    
    /**
    * 描述：建立单一querytable
     * 参数：table name
     * 返回：querytable
     */
    public QueryTable BuildSingleQueryTable(String table_name) {
        try {
            lock.readLock().lock();
            if (tables.containsKey(table_name)) {
                return new SingleTable(tables.get(table_name));
            }
        } finally {
            lock.readLock().unlock();
        }
        throw new TableNotExistException(table_name);
    }
    
    /**
     * 描述：建立复合querytable
     * 参数：table names，join逻辑
     * 返回：querytable
     */
    public QueryTable BuildJointQueryTable(ArrayList<String> table_names, Logic logic) {
        ArrayList<Table> my_tables = new ArrayList<>();
        try {
            lock.readLock().lock();
            for (String table_name : table_names) {
                if (!tables.containsKey(table_name))
                    throw new TableNotExistException(table_name);
                my_tables.add(tables.get(table_name));
            }
        } finally {
            lock.readLock().unlock();
        }
        return new JointTable(my_tables, logic);
    }

    /**
     * 描述：取消table中的所有pinned页
     * 参数：table names
     * 返回：无
     */
    public void unpinTables(ArrayList<String> table_names)
    {
        try {
            lock.writeLock().lock();
            for (String table_name : table_names) {
                if (!tables.containsKey(table_name))
                    throw new TableNotExistException(table_name);
                tables.get(table_name).unpin();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}


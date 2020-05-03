package cn.edu.thssdb.schema;

import cn.edu.thssdb.exception.DuplicateTableException;
import cn.edu.thssdb.exception.FileIOException;
import cn.edu.thssdb.query.QueryResult;
import cn.edu.thssdb.query.QueryTable;
import cn.edu.thssdb.type.ColumnType;

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
        for (Table table : tables.values())
        {
            String filename = DATA_DIRECTORY + "meta_" + name + "_" + table.tableName + ".data";
            ArrayList<Column> columns = table.columns;
            try {
                FileOutputStream  fos = new FileOutputStream(filename);
                OutputStreamWriter writer = new OutputStreamWriter(fos);
                for (Column column : columns)
                {
                    writer.write(column.toString() + "\n");
                }
                writer.close();
                fos.close();
            }
            catch (Exception e) {
                throw new FileIOException(filename);
            }
        }
    }

    public void create(String name, Column[] columns)
    {
        try {
            lock.writeLock().lock();
            if (tables.containsKey(name))
                throw new DuplicateTableException(name);

            Table newTable = new Table(this.name, name, columns);
            tables.put(name, newTable);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void drop() {
        try {
            lock.writeLock().lock();
            final String filenamePrefix = DATA_DIRECTORY + "meta_" + this.name + "_";
            final String filenameSuffix = ".data";
            for (Table table : tables.values())
            {
                File metaFile = new File(filenamePrefix + table.tableName + filenameSuffix);
                if (metaFile.isFile())
                    metaFile.delete();
                table.dropSelf();
                tables.remove(table.tableName);
            }
            tables = null;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public String select(QueryTable[] queryTables) {
        // TODO
        QueryResult queryResult = new QueryResult(queryTables);
        return null;
    }

    private void recover() {
        File dir = new File(DATA_DIRECTORY);
        File[] fileList = dir.listFiles();
        if (fileList == null)
            return;

        final String meta = "meta";
        for (File f : fileList)
        {
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
                while ((line = bufferedReader.readLine()) != null)
                {
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
            }
            catch (Exception e) {
                continue;
            }
        }
    }

    public void quit() {
        try {
            lock.writeLock().lock();
            for (Table table : tables.values())
            {
                table.persist();
            }
            persist();
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            lock.writeLock().unlock();
        }
    }
}

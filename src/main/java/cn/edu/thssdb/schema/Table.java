package cn.edu.thssdb.schema;

import cn.edu.thssdb.cache.Cache;
import cn.edu.thssdb.exception.*;
import cn.edu.thssdb.index.BPlusTree;
import static cn.edu.thssdb.utils.Global.*;

import cn.edu.thssdb.query.JointRow;
import cn.edu.thssdb.query.Logic;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.type.ResultType;
import javafx.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Table implements Iterable<Row> {
    ReentrantReadWriteLock lock;
    private String databaseName;
    public String tableName;
    public ArrayList<Column> columns;
    public Cache cache;
    //public BPlusTree<Entry, Row> index;
    private int primaryIndex = -1;

    public Table(String databaseName, String tableName, Column[] columns) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.columns = new ArrayList<>(Arrays.asList(columns));
        for (int i = 0; i < this.columns.size(); i++)
        {
            if (this.columns.get(i).getPrimary() == 1)
                primaryIndex = i;
        }
        //this.index = new BPlusTree<>();
//        System.out.println(primaryIndex);
        this.cache = new Cache(databaseName, tableName);
        this.lock = new ReentrantReadWriteLock();
        recover();
    }

    private void recover() {
        File dir = new File(DATA_DIRECTORY);
        File[] fileList = dir.listFiles();
        if (fileList == null)
            return;

        HashMap<Integer, File> pageFileList = new HashMap<>();
        int pageNum = 0;
        for (File f : fileList)
        {
            if (f != null && f.isFile())
            {
                try{

                    String[] parts = f.getName().split("\\.")[0].split("_");

                    String databaseName = parts[1];
                    String tableName = parts[2];

                    int id = Integer.parseInt(parts[3]);
                    if (!(this.databaseName.equals(databaseName) && this.tableName.equals(tableName)))
                        continue;
                    pageFileList.put(id, f);
                    if (id > pageNum)
                        pageNum = id;
                }
                catch (Exception e) {
                    continue;
                }
            }
        }

        for (int i = 1; i <= pageNum; i++)
        {

            File f = pageFileList.get(i);
            ArrayList<Row> rows = deserialize(f);
            cache.insertPage(rows, primaryIndex);
        }
    }

    public void insert(ArrayList<Column> columns, ArrayList<Entry> entries) {
        if (columns == null || entries == null)
            throw new SchemaLengthMismatchException(this.columns.size(), 0);

        // match columns and reorder entries
        int schemaLen = this.columns.size();
        if (columns.size() != schemaLen || entries.size() != schemaLen)
            throw new SchemaLengthMismatchException(schemaLen, columns.size());
        ArrayList<Entry> orderedEntries = new ArrayList<>();
        for (Column column : this.columns)
        {
            boolean isMatched = false;
            for (int i = 0; i < schemaLen; i++)
            {
                if (columns.get(i).compareTo(column) == 0)
                {
                    orderedEntries.add(entries.get(i));
                    isMatched = true;
                    break;
                }
            }
            if (!isMatched)
            {
                throw new SchemaMismatchException(column.toString());
            }
        }

        // write to cache
        try {
            lock.writeLock().lock();
            cache.insertRow(orderedEntries, primaryIndex);
        }
        catch (DuplicateKeyException e) {
            throw e;
        }
        finally {
            lock.writeLock().unlock();
        }
    }
    
    
    /**
     * 描述：将string类型的value转换成column的类型
     * 参数：column，value
     * 返回：新的值--comparable，如果不匹配会抛出异常
     */
    private Comparable ParseValue(Column the_column, String value) {
        if (value.equals("null")) {
            if (the_column.NotNull()) {
                throw new NullValueException(the_column.getName());
            }
            else {
                return null;
            }
        }
        switch (the_column.getType()) {
            case DOUBLE:
                return Double.parseDouble(value);
            case INT:
                return Integer.parseInt(value);
            case FLOAT:
                return Float.parseFloat(value);
            case LONG:
                return Long.parseLong(value);
            case STRING:
                return value.substring(1, value.length() - 1);
        }
        return null;
    }
    
    /**
     * 描述：判断value是否合法，符合column规则，这里只判断null和max length
     * 参数：column，value
     * 返回：无，如果不合法会抛出异常
     */
    private void JudgeValid(Column the_column, Comparable new_value) {
        boolean not_null = the_column.NotNull();
        ColumnType the_type = the_column.getType();
        int max_length = the_column.getMaxLength();
        if(not_null == true && new_value == null) {
            throw new NullValueException(the_column.getName());
        }
        if(the_type == ColumnType.STRING && new_value != null) {
            if(max_length >= 0 && (new_value + "").length() > max_length) {
                throw new ValueLengthExceedException(the_column.getName());
            }
        }
    }
    
    /**
     * 描述：用于sql parser的插入函数
     * 参数：column数组，value数组，都是string形式
     * 返回：无，如果不合法会抛出异常
     */
    public void insert(String[] columns, String[] values) {
        if (columns == null || values == null)
            throw new SchemaLengthMismatchException(this.columns.size(), 0);
        
        // match columns and reorder entries
        int schemaLen = this.columns.size();
        if (columns.length != schemaLen || values.length != schemaLen)
            throw new SchemaLengthMismatchException(schemaLen, columns.length);
        ArrayList<Entry> orderedEntries = new ArrayList<>();
        for (Column column : this.columns)
        {
            boolean isMatched = false;
            for (int i = 0; i < schemaLen; i++)
            {
                if (columns[i].equals(column.getName().toLowerCase()))
                {
                    Comparable the_entry_value = ParseValue(column, values[i]);
                    JudgeValid(column, the_entry_value);
                    Entry the_entry = new Entry(the_entry_value);
                    orderedEntries.add(the_entry);
                    isMatched = true;
                    break;
                }
            }
            if (!isMatched)
            {
                throw new SchemaMismatchException(column.toString());
            }
        }
        
        // write to cache
        try {
            lock.writeLock().lock();
            cache.insertRow(orderedEntries, primaryIndex);
        }
        catch (DuplicateKeyException e) {
            throw e;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void delete(Entry primaryEntry) {
        if (primaryEntry == null)
            throw new KeyNotExistException(null);

        try {
            lock.writeLock().lock();
            cache.deleteRow(primaryEntry, primaryIndex);
        }
        catch (KeyNotExistException e) {
            throw e;
        }
        finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 描述：用于sql parser的删除函数
     * 参数：c逻辑
     * 返回：字符串，表明删除了多少数据
     */
    public String delete(Logic the_logic) {
        int count = 0;
        for(Row row : this) {
            JointRow the_row = new JointRow(row, this);
            if(the_logic == null || the_logic.GetResult(the_row) == ResultType.TRUE) {
                Entry primary_entry = row.getEntries().get(primaryIndex);
                delete(primary_entry);
                count ++;
            }
        }
        return "Deleted " + count + " items.";
    }

    public void update(Entry primaryEntry, ArrayList<Column> columns, ArrayList<Entry> entries) {
//        System.out.println("updatet");
        if (primaryEntry == null || columns == null || entries == null)
            throw new KeyNotExistException(null);

        int targetKeys[] = new int[columns.size()];
        int i = 0;
        int tableColumnSize = this.columns.size();
        for (Column column : columns)
        {
            boolean isMatched = false;
            for (int j = 0; j < tableColumnSize; j++)
            {
                if (column.equals(this.columns.get(j)))
                {
                    targetKeys[i] = j;
                    isMatched = true;
                    break;
                }
            }
            if (!isMatched)
                throw new KeyNotExistException(column.toString());
            i++;
        }

        try {
            lock.writeLock().lock();
            cache.updateRow(primaryEntry, primaryIndex, targetKeys, entries);
        }
        catch (KeyNotExistException | DuplicateKeyException e) {
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 描述：用于sql parser的更新函数
     * 参数：待更新列名，待更新值（string类型），逻辑
     * 返回：字符串，表明更新了多少数据
     */
    public String update(String column_name, String value, Logic the_logic) {
        int count = 0;
        for(Row row : this) {
            JointRow the_row = new JointRow(row, this);
            if(the_logic == null || the_logic.GetResult(the_row) == ResultType.TRUE) {
                Entry primary_entry = row.getEntries().get(primaryIndex);
                //找到对应column
                boolean whether_find = false;
                Column the_column = null;
                for(Column column : this.columns) {
                    if(column.getName().equals(column_name)) {
                        the_column = column;
                        whether_find = true;
                        break;
                    }
                }
                if(the_column == null || whether_find == false) {
                    throw new AttributeNotFoundException(column_name);
                }
                
                //值处理，合法性判断
                Comparable the_entry_value = ParseValue(the_column, value);
                JudgeValid(the_column, the_entry_value);
                
                //插入
                Entry the_entry = new Entry(the_entry_value);
                ArrayList<Column> the_column_list = new ArrayList<>();
                the_column_list.add(the_column);
                ArrayList<Entry> the_entry_list = new ArrayList<>();
                the_entry_list.add(the_entry);
                update(primary_entry, the_column_list, the_entry_list);
                count ++;
            }
        }
        return "Updated " + count + " items.";
    }
    
    

    public Row get(Entry entry)
    {
        if (entry == null)
            throw new KeyNotExistException(null);

        Row row;
        try {
            lock.readLock().lock();
            row = cache.getRow(entry, primaryIndex);
        }
        catch (KeyNotExistException e) {
            throw e;
        }
        finally {
            lock.readLock().unlock();
        }
        return row;
    }

    public void persist()
    {
        try {
            lock.readLock().lock();
            cache.persist();
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void dropSelf()
    {
        try {
            lock.writeLock().lock();
            cache.dropSelf();
            cache = null;

            File dir = new File(DATA_DIRECTORY);
            File[] fileList = dir.listFiles();
            if (fileList == null)
                return;
            for (File f : fileList)
            {
                if (f != null && f.isFile())
                {
                    try {
                        String[] parts = f.getName().split("\\.")[0].split("_");
                        String databaseName = parts[1];
                        String tableName = parts[2];
                        if (!(this.databaseName.equals(databaseName) && this.tableName.equals(tableName)))
                            continue;
                    }
                    catch (Exception e) {
                        continue;
                    }
                    f.delete();
                }
            }

            columns.clear();
            columns = null;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    private ArrayList<Row> deserialize(File file) {
        ArrayList<Row> rows;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            rows = (ArrayList<Row>) ois.readObject();
            ois.close();
        }
        catch (Exception e) {
            rows = null;
        }
        return rows;
    }
    
    
    public String GetPrimaryName() {
        if(this.primaryIndex < 0 || this.primaryIndex >= this.columns.size()) {
            return null;
        }
        return this.columns.get(this.primaryIndex).getName();
    }
    
    public int GetPrimaryIndex() {
        return this.primaryIndex;
    }
    
    public String ToString() {
        String name = this.tableName;
        String top = "Column Name, Column Type, Primary, Is Null, Max Length";
        String result = "Table Name: " + name + "\n" + top + "\n";
        for(Column column : this.columns) {
            result = result + column.toString() + "\n";
        }
        return result;
    }
    
    private class TableIterator implements Iterator<Row> {
        private Iterator<Pair<Entry, Row>> iterator;
        private LinkedList<Entry> q;
        private Cache mCache;

        TableIterator(Table table) {
            mCache = table.cache;
            iterator = table.cache.getIndexIter();
            q = new LinkedList<>();
            while (iterator.hasNext())
            {
                q.add(iterator.next().getKey());
            }
            iterator = null;
        }

        @Override
        public boolean hasNext() {
            return !q.isEmpty();
        }

        @Override
        public Row next() {
            Entry entry = q.getFirst();
            Row row = mCache.getRow(entry, primaryIndex);
            q.removeFirst();
            return row;
        }

    }

    @Override
    public Iterator<Row> iterator() {
        return new TableIterator(this);
    }
    
}

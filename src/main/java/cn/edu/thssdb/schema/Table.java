package cn.edu.thssdb.schema;

import cn.edu.thssdb.cache.Cache;
import cn.edu.thssdb.exception.*;
//import cn.edu.thssdb.index.BPlusTree;
import static cn.edu.thssdb.utils.Global.*;

import cn.edu.thssdb.query.Comparer;
import cn.edu.thssdb.query.JointRow;
import cn.edu.thssdb.query.Logic;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.type.ComparerType;
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
    int tplock = 0;
    public ArrayList<Long> s_lock_list;
    public ArrayList<Long> x_lock_list;
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
        if(primaryIndex < 0 || primaryIndex >= this.columns.size()) {
            throw new PrimaryNotExistException(tableName);
        }
        //this.index = new BPlusTree<>();
//        System.out.println(primaryIndex);
        this.cache = new Cache(databaseName, tableName);
        this.lock = new ReentrantReadWriteLock();
        this.s_lock_list = new ArrayList<>();
        this.x_lock_list = new ArrayList<>();
        this.tplock = 0;
        recover();
    }

    public int get_s_lock(long session){
        int value = 0;                       //返回-1代表加锁失败  返回0代表成功但未加锁  返回1代表成功加锁
        if(tplock==2){
            if(x_lock_list.contains(session)){   //自身已经有更高级的锁了 用x锁去读，未加锁
                value = 0;
            }else{
                value = -1;                      //别的session占用x锁，未加锁
            }
        }else if(tplock==1){
            if(s_lock_list.contains(session)){    //自身已经有s锁了 用s锁去读，未加锁
                value = 0;
            }else{
                s_lock_list.add(session);         //其他session加了s锁 把自己加上
                tplock = 1;
                value = 1;
            }
        }else if(tplock==0){
            s_lock_list.add(session);              //未加锁 把自己加上
            tplock = 1;
            value = 1;
        }
        return value;
    }

    public int get_x_lock(long session){
        int value = 0;                    //返回-1代表加锁失败  返回0代表成功但未加锁  返回1代表成功加锁
        if(tplock==2){
            if(x_lock_list.contains(session)){     //自身已经取得x锁
                value = 0;
            }else{
                value = -1;                      //获取x锁失败
            }
        }else if(tplock==1){
                value = -1;                          //正在被其他s锁占用
        }else if(tplock==0){
            x_lock_list.add(session);
            tplock = 2;
            value = 1;
        }
        return value;
    }

    public void free_s_lock(long session){
        if(s_lock_list.contains(session))
        {
            s_lock_list.remove(session);
            if(s_lock_list.size()==0){
                tplock = 0;
            }else{
                tplock = 1;
            }
        }
    }

    public void free_x_lock(long session){
        if(x_lock_list.contains(session))
        {
            tplock = 0;
            x_lock_list.remove(session);
        }
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


    /**
     * 描述：插入一条记录 非transaction
     * 参数：ccolumns列表（schema顺序），entries列表（具体插入的行）
     * 返回：
     */
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
     * 描述：插入一条记录 加入transaction选项
     * 参数：ccolumns列表（schema顺序），entries列表（具体插入的行）
     * 返回：
     */
    public void insert(ArrayList<Column> columns, ArrayList<Entry> entries, boolean isTransaction) {
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
            cache.insertRow(orderedEntries, primaryIndex, isTransaction);
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
     * 描述：将comparer类型的value转换成column的类型
     * 参数：column，value
     * 返回：新的值--comparable，如果不匹配会抛出异常
     * 如果value是null,判断是否符合可空规则
     * 如果value是column，报错
     * 如果column是数字，value是string，或者相反，报错
     * 如果column和value都是数字，value强制类型转换成column类型，比如你把id：int改成5.01，那变成5
     */
    private Comparable ParseValue(Column the_column, Comparer value) {
        if (value == null || value.mValue == null ||value.mType == ComparerType.NULL) {
            if (the_column.NotNull()) {
                throw new NullValueException(the_column.getName());
            }
            else {
                return null;
            }
        }
        String string_value = value.mValue + "";
        if(value.mType == ComparerType.COLUMN) {
            if(the_column.getType().equals(ColumnType.STRING)) {
                throw new TypeNotMatchException(ComparerType.COLUMN, ComparerType.STRING);
            }
            else {
                throw new TypeNotMatchException(ComparerType.COLUMN, ComparerType.NUMBER);
            }
        }
        
        switch (the_column.getType()) {
            case DOUBLE:
                if(value.mType == ComparerType.STRING) {
                    throw new TypeNotMatchException(ComparerType.STRING, ComparerType.NUMBER);
                }
                return Double.parseDouble(string_value);
            case INT:
                if(value.mType == ComparerType.STRING) {
                    throw new TypeNotMatchException(ComparerType.STRING, ComparerType.NUMBER);
                }
                double double_value = Double.parseDouble(string_value);
                int int_value = (int)double_value;
                return Integer.parseInt(int_value + "");
            case FLOAT:
                if(value.mType == ComparerType.STRING) {
                    throw new TypeNotMatchException(ComparerType.STRING, ComparerType.NUMBER);
                }
                return Float.parseFloat(string_value);
            case LONG:
                if(value.mType == ComparerType.STRING) {
                    throw new TypeNotMatchException(ComparerType.STRING, ComparerType.NUMBER);
                }
                double double_value_2 = Double.parseDouble(string_value);
                long long_value = (long)double_value_2;
                return Long.parseLong(long_value + "");
            case STRING:
                if(value.mType == ComparerType.NUMBER) {
                    throw new TypeNotMatchException(ComparerType.STRING, ComparerType.NUMBER);
                }
                return string_value;
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
        if (columns.length > schemaLen) {
            throw new SchemaLengthMismatchException(schemaLen, columns.length);
        }
        else if(values.length > schemaLen) {
            throw new SchemaLengthMismatchException(schemaLen, values.length);
        }
        else if (columns.length != values.length) {
            throw new SchemaLengthMismatchException(columns.length, values.length);
        }
        ArrayList<Entry> orderedEntries = new ArrayList<>();
        for (Column column : this.columns)
        {
            int equal_num = 0;
            int place = -1;
            for (int i = 0; i < values.length; i++)
            {
                if (columns[i].equals(column.getName().toLowerCase()))
                {
                    place = i;
                    equal_num ++;
                }
            }
            if (equal_num > 1)
            {
                throw new DuplicateColumnException(column.toString());
            }
            Comparable the_entry_value = null;
            if (equal_num == 0 || place < 0 || place >= columns.length)
            {
                the_entry_value = null;
            }
            else{
                the_entry_value = ParseValue(column, values[place]);
            }
            JudgeValid(column, the_entry_value);
            Entry the_entry = new Entry(the_entry_value);
            orderedEntries.add(the_entry);
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
     * 描述：用于sql parser的插入函数 重载了事务
     * 参数：column数组，value数组，都是string形式
     * 返回：无，如果不合法会抛出异常
     */
    public void insert(String[] columns, String[] values, boolean isTransaction) {
        if (columns == null || values == null)
            throw new SchemaLengthMismatchException(this.columns.size(), 0);

        // match columns and reorder entries
        int schemaLen = this.columns.size();
        if (columns.length > schemaLen) {
            throw new SchemaLengthMismatchException(schemaLen, columns.length);
        }
        else if(values.length > schemaLen) {
            throw new SchemaLengthMismatchException(schemaLen, values.length);
        }
        else if (columns.length != values.length) {
            throw new SchemaLengthMismatchException(columns.length, values.length);
        }
        ArrayList<Entry> orderedEntries = new ArrayList<>();
        for (Column column : this.columns)
        {
            int equal_num = 0;
            int place = -1;
            for (int i = 0; i < values.length; i++)
            {
                if (columns[i].equals(column.getName().toLowerCase()))
                {
                    place = i;
                    equal_num ++;
                }
            }
            if (equal_num > 1)
            {
                throw new DuplicateColumnException(column.toString());
            }
            Comparable the_entry_value = null;
            if (equal_num == 0 || place < 0 || place >= columns.length)
            {
                the_entry_value = null;
            }
            else{
                the_entry_value = ParseValue(column, values[place]);
            }
            JudgeValid(column, the_entry_value);
            Entry the_entry = new Entry(the_entry_value);
            orderedEntries.add(the_entry);
        }

        // write to cache
        try {
            lock.writeLock().lock();
            cache.insertRow(orderedEntries, primaryIndex, isTransaction);
        }
        catch (DuplicateKeyException e) {
            throw e;
        }
        finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 描述：用于sql parser的插入函数
     * 参数：value数组，string形式
     * 返回：无，如果不合法会抛出异常
     */
    public void insert(String[] values) {
        if (values == null)
            throw new SchemaLengthMismatchException(this.columns.size(), 0);
    
        // match columns and reorder entries
        int schemaLen = this.columns.size();
        if(values.length > schemaLen) {
            throw new SchemaLengthMismatchException(schemaLen, values.length);
        }
        
        ArrayList<Entry> orderedEntries = new ArrayList<>();
        for (int i = 0; i < this.columns.size(); i ++)
        {
            Column column = this.columns.get(i);
            Comparable the_entry_value = null;
            if(i >= 0 && i < values.length) {
                the_entry_value = ParseValue(column, values[i]);
            }
            JudgeValid(column, the_entry_value);
            Entry the_entry = new Entry(the_entry_value);
            orderedEntries.add(the_entry);
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
     * 描述：用于sql parser的插入函数 重载了事务
     * 参数：value数组，string形式
     * 返回：无，如果不合法会抛出异常
     */
    public void insert(String[] values, boolean isTransaction) {
        if (values == null)
            throw new SchemaLengthMismatchException(this.columns.size(), 0);

        // match columns and reorder entries
        int schemaLen = this.columns.size();
        if(values.length > schemaLen) {
            throw new SchemaLengthMismatchException(schemaLen, values.length);
        }

        ArrayList<Entry> orderedEntries = new ArrayList<>();
        for (int i = 0; i < this.columns.size(); i ++)
        {
            Column column = this.columns.get(i);
            Comparable the_entry_value = null;
            if(i >= 0 && i < values.length) {
                the_entry_value = ParseValue(column, values[i]);
            }
            JudgeValid(column, the_entry_value);
            Entry the_entry = new Entry(the_entry_value);
            orderedEntries.add(the_entry);
        }

        // write to cache
        try {
            lock.writeLock().lock();
            cache.insertRow(orderedEntries, primaryIndex, isTransaction);
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

    public void delete(Entry primaryEntry, boolean isTransaction) {
        if (primaryEntry == null)
            throw new KeyNotExistException(null);

        try {
            lock.writeLock().lock();
            cache.deleteRow(primaryEntry, primaryIndex, isTransaction);
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

    /**
     * 描述：用于sql parser的删除函数 重载了事务
     * 参数：c逻辑
     * 返回：字符串，表明删除了多少数据
     */
    public String delete(Logic the_logic, boolean isTransaction) {
        int count = 0;
        for(Row row : this) {
            JointRow the_row = new JointRow(row, this);
            if(the_logic == null || the_logic.GetResult(the_row) == ResultType.TRUE) {
                Entry primary_entry = row.getEntries().get(primaryIndex);
                delete(primary_entry, isTransaction);
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

    public void update(Entry primaryEntry, ArrayList<Column> columns, ArrayList<Entry> entries, boolean isTransaction) {
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
            cache.updateRow(primaryEntry, primaryIndex, targetKeys, entries, isTransaction);
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
    public String update(String column_name, Comparer value, Logic the_logic) {
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

    /**
     * 描述：用于sql parser的更新函数 重载了事务的版本
     * 参数：待更新列名，待更新值（string类型），逻辑
     * 返回：字符串，表明更新了多少数据
     */
    public String update(String column_name, Comparer value, Logic the_logic, boolean isTransaction) {
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
                update(primary_entry, the_column_list, the_entry_list, isTransaction);
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
    
    /**
     * 描述：获得主键名称
     * 参数：无
     * 返回：主键名称，如果没有就null
     */
    public String GetPrimaryName() {
        if(this.primaryIndex < 0 || this.primaryIndex >= this.columns.size()) {
            return null;
        }
        return this.columns.get(this.primaryIndex).getName();
    }
    
    public int GetPrimaryIndex() {
        return this.primaryIndex;
    }
    
    /**
     * 描述：显示整个表信息
     * 参数：无
     * 返回：string，表信息
     */
    public String ToString() {
        String name = this.tableName;
        String top = "Column Name, Column Type, Primary, Is Null, Max Length";
        String result = "Table Name: " + name + "\n" + top + "\n";
        for(Column column : this.columns) {
            if(column == null) {
                continue;
            }
            result = result + column.toString() + "\n";
        }
        return result;
    }

    /**
     * 描述：取消整个表中所有pinned的页
     * 参数：无
     * 返回：无
     */
    public void unpin()
    {
        cache.unpin();
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

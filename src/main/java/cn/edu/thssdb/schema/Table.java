package cn.edu.thssdb.schema;

import cn.edu.thssdb.cache.Cache;
import cn.edu.thssdb.exception.KeyNotExistException;
import cn.edu.thssdb.exception.SchemaLengthMismatchException;
import cn.edu.thssdb.exception.SchemaMismatchException;
import cn.edu.thssdb.index.BPlusTree;
import static cn.edu.thssdb.utils.Global.*;
import javafx.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Table implements Iterable<Row> {
    ReentrantReadWriteLock lock;
    private String databaseName;
    public String tableName;
    public ArrayList<Column> columns;
    private Cache cache;
    //public BPlusTree<Entry, Row> index;
    private int primaryIndex;

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
        System.out.println(primaryIndex);
        this.cache = new Cache(databaseName, tableName);
        this.lock = new ReentrantReadWriteLock();
        recover();
    }

    private void recover() {
        File dir = new File(DATA_DIRECTORY);
        File[] fileList = dir.listFiles();
        if (fileList == null)
            return;
        for (File f : fileList)
        {
            if (f != null && f.isFile())
            {
                try{
                    String databaseName = f.getName().split("-")[0];
                    String tableName = f.getName().split("-")[1];
                    if (!(this.databaseName.equals(databaseName) && this.tableName.equals(tableName)))
                        continue;
                }
                catch (Exception e) {
                    continue;
                }

                ArrayList<Row> rows = deserialize(f);
                cache.insertPage(rows, primaryIndex);
            }
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

    public void update(Entry primaryEntry, ArrayList<Column> columns, ArrayList<Entry> entries) {
        if (primaryEntry == null || columns == null || entries == null)
            throw new KeyNotExistException(null);

        int targetKeys[] = new int[columns.size()];
        int i = 0;
        int tableColumnSize = this.columns.size();
        for (Column column : columns)
        {
            boolean isMatched = false;
            for (int j = 0; j < tableColumnSize; i++)
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
        catch (KeyNotExistException e) {
            throw e;
        }
        finally {
            lock.writeLock().unlock();
        }
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

    private class TableIterator implements Iterator<Row> {
        private Iterator<Pair<Entry, Row>> iterator;
        private Cache mCache;

        TableIterator(Table table) {
            mCache = table.cache;
            this.iterator = table.cache.getIndexIter();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Row next() {
            Entry entry = iterator.next().getKey();
            Row row = mCache.getRow(entry, primaryIndex);
            return row;
        }
    }

    @Override
    public Iterator<Row> iterator() {
        return new TableIterator(this);
    }
}

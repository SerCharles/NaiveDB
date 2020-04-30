package cn.edu.thssdb.schema;

import cn.edu.thssdb.cache.Cache;
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
    public BPlusTree<Entry, Row> index;
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
        this.index = new BPlusTree<>();
        this.cache = new Cache(index, databaseName, tableName);
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

    public void insert() {
        // TODO
    }

    public void delete() {
        // TODO
    }

    public void update() {
        // TODO
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

        TableIterator(Table table) {
            this.iterator = table.index.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Row next() {
            return iterator.next().getValue();
        }
    }

    @Override
    public Iterator<Row> iterator() {
        return new TableIterator(this);
    }
}

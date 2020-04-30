package cn.edu.thssdb.schema;

import cn.edu.thssdb.cache.Cache;
import cn.edu.thssdb.index.BPlusTree;
import javafx.util.Pair;

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
    private int primaryKey;

    public Table(String databaseName, String tableName, Column[] columns) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.columns = new ArrayList<>(Arrays.asList(columns));
        this.index = new BPlusTree<>();
        this.cache = new Cache(index, databaseName, tableName);
        this.lock = new ReentrantReadWriteLock();
        recover();
    }

    private void recover() {
        // TODO
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

    private void serialize() {
        // TODO
    }

    private ArrayList<Row> deserialize() {
        // TODO
        return null;
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

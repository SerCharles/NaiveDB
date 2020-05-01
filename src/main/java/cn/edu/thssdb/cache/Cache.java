package cn.edu.thssdb.cache;

import cn.edu.thssdb.exception.KeyNotExistException;
import cn.edu.thssdb.index.BPlusTree;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Cache {
    private static final int maxPageNum = 100;
    private HashMap<Integer, Page> pages;
    private int pageNum;
    private BPlusTree<Entry, Row> index;
    private String cacheName;

    public Cache(BPlusTree<Entry, Row> index, String databaseName, String tableName)
    {
        pageNum = 0;
        this.cacheName = databaseName + "_" + tableName;
        this.index = index;
    }

    public int getPageNum() { return pageNum; }

    public Iterator<Pair<Entry, Row>> getIndexIter() { return index.iterator(); }

    public boolean insertPage(ArrayList<Row> rows, int primaryKey)
    {
        boolean noOverflow = addPage();
        Page curPage = pages.get(pageNum);
        for (Row row : rows)
        {
            ArrayList<Entry> entries = row.getEntries();
            Entry primaryEntry = entries.get(primaryKey);
            int len = row.toString().length();
            curPage.insertEntry(primaryEntry, len);
        }
        return noOverflow;
    }

    public void insertRow(ArrayList<Entry> entries, int primaryKey)
    {
        Row row = new Row(entries.toArray(new Entry[0]));
        int len = row.toString().length();
        Entry primaryEntry = entries.get(primaryKey);
        Page curPage = pages.get(pageNum);
        if (curPage == null || curPage.getSize() + len > Page.maxSize)
        {
            addPage();
            curPage = pages.get(pageNum);
        }
        curPage.insertEntry(primaryEntry, len);
        curPage.setEdited(true);
        curPage.setTimeStamp();
    }

    public void deleteRow(Entry entry, int primaryKey)
    {
        Row row;
        try {
            row = index.get(entry);
        }
        catch (KeyNotExistException e) {
            throw new KeyNotExistException(entry.toString());
        }

        int position = row.getPosition();
        if (row instanceof EmptyRow)
        {
            exchangePage(position, primaryKey);
            row = index.get(entry);
        }

        index.remove(entry);
        Page curPage = pages.get(position);
        curPage.removeEntry(entry, row.toString().length());
        curPage.setTimeStamp();
        curPage.setEdited(true);
    }

    public void updateRow(Entry primaryEntry, int primaryKey,
                          int[] targetKeys, ArrayList<Entry> targetEntries)
    {
        Row row;
        try {
            row = index.get(primaryEntry);
        }
        catch (KeyNotExistException e) {
            throw new KeyNotExistException(primaryEntry.toString());
        }

        int position = row.getPosition();
        if (row instanceof EmptyRow)
        {
            exchangePage(position, primaryKey);
            row = index.get(primaryEntry);
        }

        boolean changePrimaryEntry = false;
        Entry targetPrimaryEntry = null;
        int originalLen = row.toString().length();
        for (int i = 0; i < targetKeys.length; i++)
        {
            int key = targetKeys[i];
            if (key == primaryKey)
            {
                changePrimaryEntry = true;
                targetPrimaryEntry = targetEntries.get(i);
            }
            row.getEntries().set(key, targetEntries.get(i));
        }

        Page curPage = pages.get(position);
        if (changePrimaryEntry)
        {
            curPage.removeEntry(primaryEntry, originalLen);
            curPage.insertEntry(targetPrimaryEntry, row.toString().length());
        }
        curPage.setTimeStamp();
        curPage.setEdited(true);
    }

    public Row getRow(Entry entry, int primaryKey)
    {
        Row row;
        try {
            row = index.get(entry);
        }
        catch (KeyNotExistException e) {
            throw new KeyNotExistException(entry.toString());
        }

        if (row instanceof EmptyRow)
        {
            int position = row.getPosition();
            exchangePage(position, primaryKey);
            return index.get(entry);
        }
        else
        {
            pages.get(row.getPosition()).setTimeStamp();
            return row;
        }
    }

    private void exchangePage(int pageId, int primaryKey)
    {
        if (pageNum >= maxPageNum)
            expelPage();

        Page curPage = new Page(cacheName, pageId);
        ArrayList<Row> rows = deserialize(new File(curPage.getPageFileName()));
        for (Row row : rows)
        {
            row.setPosition(pageId);
            Entry primaryEntry = row.getEntries().get(primaryKey);
            index.put(primaryEntry, row);
            curPage.insertEntry(primaryEntry, row.toString().length());
        }
        pages.put(pageId, curPage);
    }

    private boolean addPage()
    {
        boolean noOverflow = true;
        if (pageNum >= maxPageNum)
        {
            expelPage();
            noOverflow = false;
        }
        pageNum++;
        Page page = new Page(cacheName, pageNum);
        pages.put(pageNum, page);
        return noOverflow;
    }

    private void expelPage()
    {
        long earliest = Long.MAX_VALUE;
        int targetID = 0;
        Iterator<HashMap.Entry<Integer, Page>> iter = pages.entrySet().iterator();
        while (iter.hasNext())
        {
            Page page = iter.next().getValue();
            long timeStamp = page.getTimeStamp();
            if (timeStamp <= earliest)
            {
                earliest = timeStamp;
                targetID = page.getId();
            }
        }

        Page page = pages.get(targetID);
        if (page == null)
            return;
        ArrayList<Row> rows = new ArrayList<>();
        ArrayList<Entry> entries = page.getEntries();
        for (Entry entry : entries)
        {
            rows.add(index.get(entry));
            index.put(entry, this.new EmptyRow(targetID));
        }
        if (page.getEdited())
        {
            // rewrite to disk
            try {
                serialize(rows, page.getPageFileName());
            }
            catch (IOException e)
            {
                return;
            }

        }
        pages.remove(targetID);
    }

    private void serialize(ArrayList<Row> rows, String filename) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
        oos.writeObject(rows);
        oos.close();
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

    public class EmptyRow extends Row {
        public EmptyRow(int position)
        {
            super();
            this.position = position;
        }
    }

}

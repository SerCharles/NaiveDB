package cn.edu.thssdb.cache;

import cn.edu.thssdb.exception.DuplicateKeyException;
import cn.edu.thssdb.exception.KeyNotExistException;
import cn.edu.thssdb.index.BPlusTree;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import javafx.scene.chart.ScatterChart;
import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static cn.edu.thssdb.utils.Global.DATA_DIRECTORY;

public class Cache {
    private static final int maxPageNum = 1000;
    private HashMap<Integer, Page> pages;
    private int pageNum;
    private BPlusTree<Entry, Row> index;
    private String cacheName;

    public Cache(String databaseName, String tableName)
    {
        pageNum = 0;
        this.cacheName = databaseName + "_" + tableName;
        this.index = new BPlusTree<>();
        pages = new HashMap<>();
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
            row.setPosition(pageNum);
            if (noOverflow)
                index.put(primaryEntry, row);
            else
                index.put(primaryEntry, this.new EmptyRow(pageNum));
        }
        return noOverflow;
    }


    /**
     * 描述：向缓存插入一条信息
     * 参数：主键编号和行信息
     * 返回：
     */
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
        row.setPosition(pageNum);
        try {
            index.put(primaryEntry, row);
        }
        catch (DuplicateKeyException e) {
            curPage.setTimeStamp();
            throw new DuplicateKeyException(primaryEntry.toString());
        }
        curPage.insertEntry(primaryEntry, len);
        curPage.setEdited(true);
        curPage.setTimeStamp();
    }

    /**
     * 描述：向缓存插入一条信息
     * 参数：主键编号和行信息 重载后加入是否处于事务中的选项
     * 返回：
     */
    public void insertRow(ArrayList<Entry> entries, int primaryKey, boolean isTransaction)
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
        row.setPosition(pageNum);
        try {
            index.put(primaryEntry, row);
        }
        catch (DuplicateKeyException e) {
            curPage.setTimeStamp();
            throw new DuplicateKeyException(primaryEntry.toString());
        }
        curPage.insertEntry(primaryEntry, len);
        if (isTransaction)
            curPage.setPinned(true);
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

    public void deleteRow(Entry entry, int primaryKey, boolean isTransaction)
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
        if (isTransaction)
            curPage.setPinned(true);
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
            if (targetKeys[i] == primaryKey)
            {
                changePrimaryEntry = true;
                targetPrimaryEntry = targetEntries.get(i);
                // check if duplicated
                if (index.contains(targetPrimaryEntry) && !primaryEntry.equals(targetPrimaryEntry))
                    throw new DuplicateKeyException(targetPrimaryEntry.toString());
                break;
            }
        }

        // update row
        for (int i = 0; i < targetKeys.length; i++)
        {
            int key = targetKeys[i];
            row.getEntries().set(key, targetEntries.get(i));
        }

        Page curPage = pages.get(position);
        if (changePrimaryEntry)
        {
            curPage.removeEntry(primaryEntry, originalLen);
            curPage.insertEntry(targetPrimaryEntry, row.toString().length());
            index.remove(primaryEntry);
            try {
                index.put(targetPrimaryEntry, row);
            }
            catch (DuplicateKeyException e) {
                throw new DuplicateKeyException(targetPrimaryEntry.toString());
            }
        }
        curPage.setTimeStamp();
        curPage.setEdited(true);
    }

    public void updateRow(Entry primaryEntry, int primaryKey,
                          int[] targetKeys, ArrayList<Entry> targetEntries,
                          boolean isTransaction)
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
            if (targetKeys[i] == primaryKey)
            {
                changePrimaryEntry = true;
                targetPrimaryEntry = targetEntries.get(i);
                // check if duplicated
                if (index.contains(targetPrimaryEntry) && !primaryEntry.equals(targetPrimaryEntry))
                    throw new DuplicateKeyException(targetPrimaryEntry.toString());
                break;
            }
        }

        // update row
        for (int i = 0; i < targetKeys.length; i++)
        {
            int key = targetKeys[i];
            row.getEntries().set(key, targetEntries.get(i));
        }

        Page curPage = pages.get(position);
        if (changePrimaryEntry)
        {
            curPage.removeEntry(primaryEntry, originalLen);
            curPage.insertEntry(targetPrimaryEntry, row.toString().length());
            index.remove(primaryEntry);
            try {
                index.put(targetPrimaryEntry, row);
            }
            catch (DuplicateKeyException e) {
                throw new DuplicateKeyException(targetPrimaryEntry.toString());
            }
        }
        curPage.setTimeStamp();
        if (isTransaction)
            curPage.setPinned(true);
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

    public void persist()
    {
        for (Page page : pages.values())
        {
            ArrayList<Row> rows = new ArrayList<>();
            for (Entry entry : page.getEntries())
            {
                rows.add(index.get(entry));
            }

            try {
                serialize(rows, DATA_DIRECTORY + page.getPageFileName());
            }
            catch (IOException e)
            {
                return;
            }
        }
    }

    public void dropSelf()
    {
        for (Page page : pages.values())
        {
            page.getEntries().clear();
        }
        pages.clear();

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // maybe should delete element one by one?
        index = null;
    }

    public void unpin()
    {
        for (Page page : pages.values())
            page.setPinned(false);
    }

    private void exchangePage(int pageId, int primaryKey)
    {
        if (pageNum >= maxPageNum)
            expelPage();

        Page curPage = new Page(cacheName, pageId);
        ArrayList<Row> rows = deserialize(new File(DATA_DIRECTORY + curPage.getPageFileName()));
        for (Row row : rows)
        {
            row.setPosition(pageId);
            Entry primaryEntry = row.getEntries().get(primaryKey);
            index.update(primaryEntry, row);
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
            boolean isPinned = page.getPinned();
            if (timeStamp <= earliest && !isPinned)
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
            index.update(entry, this.new EmptyRow(targetID));
        }
        if (page.getEdited())
        {
            // rewrite to disk
            try {
                serialize(rows, DATA_DIRECTORY + page.getPageFileName());
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
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            rows = (ArrayList<Row>) ois.readObject();
        }
        catch (Exception e) {
            rows = null;
        }
        finally {
            try {
                ois.close();
            }
            catch (Exception e){
                return null;
            }
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

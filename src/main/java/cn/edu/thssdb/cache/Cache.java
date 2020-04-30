package cn.edu.thssdb.cache;

import cn.edu.thssdb.index.BPlusTree;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;

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
        if (page.getEdited())
        {
            // rewrite to disk
        }
        ArrayList<Entry> entries = page.getEntries();
        for (Entry entry : entries)
        {
            index.put(entry, page.new EmptyRow(targetID));
        }
        pages.remove(targetID);
    }

}

package cn.edu.thssdb.cache;

import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;

import java.util.ArrayList;


public class Page {
    private static final int maxSize = 1024;
    private int id;               // unique id
    private int size;             // size of the page
    private ArrayList<Entry> entries;
    private String pageFileName;  // filename on the disk
    private long timeStamp;       // timestamp of last visit
    private Boolean edited;       // this page has been edited, need to be write back to file

    public Page(String upperName, int id)
    {
        pageFileName = "page_" + upperName + "_" + id + ".data";
        this.id = id;
        size = 0;
        edited = false;
        timeStamp = System.currentTimeMillis();
    }

    public class EmptyRow extends Row {
        private int position;

        public EmptyRow(int position)
        {
            super();
            this.position = position;
        }

        public int getPosition() { return position; }
    }

    public int getId() { return id; }

    public void setTimeStamp(long timeStamp) { this.timeStamp = timeStamp; }
    public long getTimeStamp() { return timeStamp; }

    public void setEdited(Boolean isEdited) { this.edited = isEdited; }
    public Boolean getEdited() { return this.edited; }

    public ArrayList<Entry> getEntries() { return entries; }

    public void insertEntry(Entry entry, int len)
    {
        size += len;
        entries.add(entry);
    }
}

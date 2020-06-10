package cn.edu.thssdb.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class Row implements Serializable {
    private static final long serialVersionUID = -5809782578272943999L;
    protected ArrayList<Entry> entries;
    protected int position;

    public Row() {
        this.entries = new ArrayList<>();
        position = 0;
    }

    public Row(Entry[] entries) {
        this.entries = new ArrayList<>(Arrays.asList(entries));
        position = 0;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    public int getPosition() { return position; }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public void appendEntries(ArrayList<Entry> entries) {
        this.entries.addAll(entries);
    }

    public String toString() {
        if (entries == null)
            return "EMPTY";
        StringJoiner sj = new StringJoiner(", ");
        for (Entry e : entries)
            sj.add(e.toString());
        return sj.toString();
    }
    
    public ArrayList<String> toStringList() {
        ArrayList<String> result = new ArrayList<>();
        if (entries == null)
            return result;
        for (Entry e : entries)
            result.add(e.toString());
        return result;
    }
}

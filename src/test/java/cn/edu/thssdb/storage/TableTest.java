package cn.edu.thssdb.storage;

import cn.edu.thssdb.exception.DuplicateKeyException;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.type.ColumnType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TableTest {
    public static Table table;
    public static ArrayList<Column> columns;

    @BeforeClass
    public static void setUp()
    {
        Column c[] = {new Column("id", ColumnType.INT, 1, true, 10),
                new Column("name", ColumnType.STRING, 0, true, 10),
                new Column("dept", ColumnType.STRING, 0, true, 10),
                new Column("age", ColumnType.INT, 0, true, 10),
        };
        columns = new ArrayList<>(Arrays.asList(c));
        table = new Table("University", "student", c);

        Entry dept = new Entry("THSS");
        for (int i = 0; i < 2000; i++)
        {
            Entry id = new Entry(i);
            Entry name = new Entry("" + i);
            Entry age = new Entry(i % 22);
            ArrayList<Entry> entries = new ArrayList<>();
            entries.add(id);
            entries.add(name);
            entries.add(dept);
            entries.add(age);
            table.insert(columns, entries);
        }
    }

    @Test
    public void testRecover()
    {

    }

    @Test
    public void testInsert()
    {
        int id;
        Row r;
        ArrayList<Entry> entries;

        id = 66;
        r = table.get(new Entry(id));
        entries = r.getEntries();
        assertEquals(entries.get(1), new Entry("" + id));
        assertEquals(entries.get(3), new Entry(id % 22));

        id = 666;
        r = table.get(new Entry(id));
        entries = r.getEntries();
        assertEquals(entries.get(1), new Entry("" + id));
        assertEquals(entries.get(3), new Entry(id % 22));

        id = 1666;
        r = table.get(new Entry(id));
        entries = r.getEntries();
        assertEquals(entries.get(1), new Entry("" + id));
        assertEquals(entries.get(3), new Entry(id % 22));

        boolean causedException = false;
        try {
            table.insert(columns, entries);
        }
        catch (DuplicateKeyException e) {
            causedException = true;
        }
        catch (Exception e) {
            System.out.println("big exception");
        }
        finally {
            assertTrue(causedException);
        }
    }

    @Test
    public void testIterate()
    {

    }

    @Test
    public void testDelete()
    {

    }

    @Test
    public void testUpdate()
    {

    }
}

package cn.edu.thssdb.storage;

import cn.edu.thssdb.cache.Cache;
import cn.edu.thssdb.exception.DuplicateKeyException;
import cn.edu.thssdb.exception.KeyNotExistException;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.type.ColumnType;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static cn.edu.thssdb.utils.Global.DATA_DIRECTORY;
import static org.junit.Assert.*;


@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
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

//        System.out.println(table.cache.getPageNum());
    }

    @Test
    public void test02Insert()
    {
        int id;
        Row r;
        ArrayList<Entry> entries;
        boolean causedException = false;

        id = 66;
        r = table.get(new Entry(id));
        entries = r.getEntries();
        assertEquals(entries.get(1), new Entry("" + id));
        assertEquals(entries.get(3), new Entry(id % 22));

        causedException = false;
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

        id = 666;
        r = table.get(new Entry(id));
        entries = r.getEntries();
        assertEquals(entries.get(1), new Entry("" + id));
        assertEquals(entries.get(3), new Entry(id % 22));

        id = 132;
        r = table.get(new Entry(id));
        entries = r.getEntries();
        assertEquals(entries.get(1), new Entry("" + id));
        assertEquals(entries.get(3), new Entry(id % 22));

        id = 1666;
        r = table.get(new Entry(id));
        entries = r.getEntries();
        assertEquals(entries.get(1), new Entry("" + id));
        assertEquals(entries.get(3), new Entry(id % 22));

        causedException = false;
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
    public void test02Iterate()
    {
        Iterator<Row> iter = table.iterator();
        for (int i = 0; i < 2000; i++)
        {
            if (iter.hasNext())
            {
                Row r = iter.next();
                Entry primary = r.getEntries().get(0);
                assertEquals(primary, new Entry(i));
            }
            else
            {
                assertTrue(false);
            }
        }
    }

    @Test
    public void test03Update()
    {
        int id, nid;
        ArrayList<Entry> entries;
        Row r;
        boolean hasDuplicate = false;

        entries = new ArrayList<>();
        id = 514;
        entries.add(new Entry(id));
        entries.add(new Entry("" + (id * 2)));
        entries.add(new Entry("THSS"));
        entries.add(new Entry(99));
        table.update(new Entry(id), columns, entries);
        r = table.get(new Entry(id));
        assertEquals(r.getEntries().get(1), new Entry("" + (id * 2)));
        assertEquals(r.getEntries().get(3), new Entry(99));

        entries = new ArrayList<>();
        id = 1410;
        entries.add(new Entry(id));
        entries.add(new Entry("" + (id - 2)));
        entries.add(new Entry("THSS"));
        entries.add(new Entry(199));
        table.update(new Entry(id), columns, entries);
        r = table.get(new Entry(id));
        assertEquals(r.getEntries().get(1), new Entry("" + (id - 2)));
        assertEquals(r.getEntries().get(3), new Entry(199));

        entries = new ArrayList<>();
        id = 17;
        nid = 5000;
        entries.add(new Entry(nid));
        entries.add(new Entry("" + (nid)));
        entries.add(new Entry("THSS"));
        entries.add(new Entry(22));
        table.update(new Entry(id), columns, entries);
        r = table.get(new Entry(nid));
        assertEquals(r.getEntries().get(1), new Entry("" + (nid)));
        assertEquals(r.getEntries().get(3), new Entry(22));

        entries = new ArrayList<>();
        hasDuplicate = false;
        id = 1897;
        nid = 888;
        entries.add(new Entry(nid));
        entries.add(new Entry("" + (nid)));
        entries.add(new Entry("THSS"));
        entries.add(new Entry(22));
        try {
            table.update(new Entry(id), columns, entries);
        }
        catch (DuplicateKeyException e) {
            hasDuplicate = true;
        }
        catch (Exception e) {
            System.out.println("big exception" + e.getClass());
        }
        finally {
            assertTrue(hasDuplicate);
            r = table.get(new Entry(id));
            assertEquals(r.getEntries().get(1), new Entry("" + id));
        }

        entries = new ArrayList<>();
        hasDuplicate = false;
        id = 17;
        nid = 888;
        entries.add(new Entry(nid));
        entries.add(new Entry("" + (nid)));
        entries.add(new Entry("THSS"));
        entries.add(new Entry(22));
        try {
            table.update(new Entry(id), columns, entries);
        }
        catch (KeyNotExistException e) {
            hasDuplicate = true;
        }
        catch (Exception e) {
            System.out.println("big exception" + e.getClass());
        }
        finally {
            assertTrue(hasDuplicate);
        }

    }

    @Test
    public void test04Delete()
    {
        int id;
        boolean existed = true;

        id = 99;
        existed = true;
        table.delete(new Entry(id));
        try {
            table.get(new Entry(id));
        }
        catch (KeyNotExistException e) {
            existed = false;
        }
        catch (Exception e) {
            System.out.println("big exception");
        }
        finally {
            assertFalse(existed);
        }

        id = 199;
        existed = true;
        table.delete(new Entry(id));
        try {
            table.get(new Entry(id));
        }
        catch (KeyNotExistException e) {
            existed = false;
        }
        catch (Exception e) {
            System.out.println("big exception");
        }
        finally {
            assertFalse(existed);
        }

        id = 1799;
        existed = true;
        table.delete(new Entry(id));
        try {
            table.get(new Entry(id));
        }
        catch (KeyNotExistException e) {
            existed = false;
        }
        catch (Exception e) {
            System.out.println("big exception");
        }
        finally {
            assertFalse(existed);
        }
    }

    @Test
    public void test05Recover()
    {
        table.persist();
        table = new Table("University", "student", columns.toArray(new Column[0]));

        int id;
        Row r;
        ArrayList<Entry> entries;
        boolean existed = true;

        id = 1599;
        r = table.get(new Entry(id));
        entries = r.getEntries();
        assertEquals(entries.get(1), new Entry("" + id));
        assertEquals(entries.get(3), new Entry(id % 22));

        id = 5000;
        r = table.get(new Entry(id));
        entries = r.getEntries();
        assertEquals(entries.get(1), new Entry("" + id));
        assertEquals(entries.get(3), new Entry(22));

        id = 1799;
        existed = true;
        try {
            table.get(new Entry(id));
        }
        catch (KeyNotExistException e) {
            existed = false;
        }
        catch (Exception e) {
            System.out.println("big exception");
        }
        finally {
            assertFalse(existed);
        }
    }

    @AfterClass
    public static void cleanUp()
    {
        //table.persist();
        File dir = new File(DATA_DIRECTORY);
        File[] fileList = dir.listFiles();
        String gitignore = ".gitignore";
        for (File f : fileList)
        {
            if (!gitignore.equals(f.getName()))
                f.delete();
        }

    }
}

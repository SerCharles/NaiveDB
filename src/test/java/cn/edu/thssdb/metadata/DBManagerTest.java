package cn.edu.thssdb.metadata;

import cn.edu.thssdb.exception.DatabaseNotExistException;
import cn.edu.thssdb.exception.KeyNotExistException;
import cn.edu.thssdb.exception.TableNotExistException;
import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.type.ColumnType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static cn.edu.thssdb.utils.Global.DATA_DIRECTORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class DBManagerTest {
    public static Manager manager;
    public static ArrayList<Column> columns;

    @BeforeClass
    public static void setUp() {
        manager = Manager.getInstance();
    }

    public void insertRecords(Database db)
    {
        Table stu = db.get("student");
        Table teacher = db.get("teacher");
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
            stu.insert(columns, entries);
            teacher.insert(columns, entries);
        }
    }

    @Test
    public void test01Create()
    {
        manager.createDatabaseIfNotExists("University");
        manager.createDatabaseIfNotExists("HighSchool");
        manager.createDatabaseIfNotExists("MiddleSchool");

        Column c[] = {new Column("id", ColumnType.INT, 1, true, 10),
                new Column("name", ColumnType.STRING, 0, true, 10),
                new Column("dept", ColumnType.STRING, 0, true, 10),
                new Column("age", ColumnType.INT, 0, true, 10),
        };
        columns = new ArrayList<>(Arrays.asList(c));

        Database university = manager.get("University");
        university.create("student", c);
        university.create("teacher", c);
        insertRecords(university);

        Database highSchool = manager.get("HighSchool");
        highSchool.create("student", c);
        highSchool.create("teacher", c);
        insertRecords(highSchool);

        Database midSchool = manager.get("MiddleSchool");
        midSchool.create("student", c);
        midSchool.create("teacher", c);
        insertRecords(midSchool);
    }

    @Test
    public void test02Delete()
    {
        Database university = manager.get("University");
        university.drop("teacher");
        boolean existed = true;
        try {
            university.get("teacher");
        }
        catch (TableNotExistException e) {
            existed = false;
        }
        finally {
            assertFalse(existed);
        }

        Table uni_stu = university.get("student");
        for (int i = 0; i < 1000; i++)
            uni_stu.delete(new Entry(i));

        manager.deleteDatabase("MiddleSchool");
        existed = true;
        try {
            manager.get("MiddleSchool");
        }
        catch (DatabaseNotExistException e) {
            existed = false;
        }
        finally {
            assertFalse(existed);
        }
    }

    @Test
    public void test03PersistRecover()
    {
        // persist
        manager.quit();

        // recover
        manager.recover();

        Database university = manager.get("University");
        boolean existed = true;
        try {
            university.get("teacher");
        }
        catch (TableNotExistException e) {
            existed = false;
        }
        finally {
            assertFalse(existed);
        }

        existed = true;
        for (int i = 0; i < 1000; i++)
        {
            try {
                Table stu = university.get("student");
                stu.get(new Entry(i));
            }
            catch (KeyNotExistException e) {
                existed = false;
            }
            finally {
                assertFalse(existed);
            }
        }

        existed = true;
        try {
            manager.get("MiddleSchool");
        }
        catch (DatabaseNotExistException e) {
            existed = false;
        }
        finally {
            assertFalse(existed);
        }

        Row r = manager.get("HighSchool").get("teacher").get(new Entry(666));
        assertEquals(r.getEntries().get(3), new Entry(666 % 22));
    }

    @AfterClass
    public static void cleanUp()
    {
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
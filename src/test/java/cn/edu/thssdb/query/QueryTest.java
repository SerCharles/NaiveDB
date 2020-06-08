package cn.edu.thssdb.query;

import cn.edu.thssdb.parser.SQLHandler;
import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.query.JointRow;
import cn.edu.thssdb.type.ComparerType;
import cn.edu.thssdb.type.ConditionType;
import cn.edu.thssdb.type.LogicType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class QueryTest {
    public static Manager manager;
    
    @BeforeClass
    public static void setUp() {
        manager = Manager.getInstance();
    }
    
    /**
    *描述：测试logic，condition，jointrow这几个类，也就是测试逻辑匹配部分
     */
    @Test
    public void LogicTest()
    {
        manager.createDatabaseIfNotExists("University");
        
        Column student[] = {new Column("id", ColumnType.INT, 1, true, 10),
                new Column("name", ColumnType.STRING, 0, true, 10),
                new Column("dept", ColumnType.STRING, 0, true, 10),
                new Column("age", ColumnType.INT, 0, true, 10),
        };
        
        Column grade[] = {new Column("id", ColumnType.INT, 1, true, 10),
                new Column("GPA", ColumnType.DOUBLE, 0, true, 10),
                new Column("rank", ColumnType.INT, 0, true, 10),
        };
        
        Column department[] = {new Column("dept_name", ColumnType.STRING, 1, true, 10),
                new Column("involution", ColumnType.DOUBLE, 0, true, 10),
        };
        
        ArrayList<Column> column1 = new ArrayList<>(Arrays.asList(student));
        ArrayList<Column> column2 = new ArrayList<>(Arrays.asList(grade));
        ArrayList<Column> column3 = new ArrayList<>(Arrays.asList(department));
        
        
        Table table1 = new Table("University", "student", student);
        Table table2 = new Table("University", "grade", grade);
        Table table3 = new Table("University", "department", department);
        
        Entry id1 = new Entry(1);
        Entry name1 = new Entry("sgl");
        Entry dept1 = new Entry("THSS");
        Entry age1 = new Entry(21);
        Entry[] entries1 = {id1, name1, dept1, age1};
        Row row1 = new Row(entries1);
        
        Entry id2 = new Entry(2);
        Entry name2 = new Entry("lsj");
        Entry dept2 = new Entry("THSS");
        Entry age2 = new Entry(20);
        Entry[] entries2 = {id2, name2, dept2, age2};
        Row row2 = new Row(entries2);
        
        Entry id3 = new Entry(3);
        Entry name3 = new Entry("borkball");
        Entry dept3 = new Entry("CST");
        Entry age3 = new Entry(21);
        Entry[] entries3 = {id3, name3, dept3, age3};
        Row row3 = new Row(entries3);
        
        
        Entry id4 = new Entry(1);
        Entry gpa4 = new Entry(3.81);
        Entry rank4 = new Entry(8);
        Entry[] entries4 = {id4, gpa4, rank4};
        Row row4 = new Row(entries4);
        
        Entry id5 = new Entry(2);
        Entry gpa5 = new Entry(3.71);
        Entry rank5 = new Entry(18);
        Entry[] entries5 = {id5, gpa5, rank5};
        Row row5 = new Row(entries5);
        
        Entry id6 = new Entry(3);
        Entry gpa6 = new Entry(3.61);
        Entry rank6 = new Entry(28);
        Entry[] entries6 = {id6, gpa6, rank6};
        Row row6 = new Row(entries6);
        
        Entry name7 = new Entry("THSS");
        Entry invo7 = new Entry(60.4);
        Entry[] entries7 = {name7, invo7};
        Row row7 = new Row(entries7);
        
        Entry name8 = new Entry("CST");
        Entry invo8 = new Entry(99.9);
        Entry[] entries8 = {name8, invo8};
        Row row8 = new Row(entries8);
        
        
        
        Comparer name_comp = new Comparer(ComparerType.COLUMN, "name");
        Comparer sgl_comp = new Comparer(ComparerType.STRING, "sgl");
        Comparer id_comp = new Comparer(ComparerType.COLUMN, "id");
        Comparer student_id_comp = new Comparer(ComparerType.COLUMN, "student.id");
        Comparer grade_id_comp = new Comparer(ComparerType.COLUMN, "grade.id");
        Comparer dept_comp = new Comparer(ComparerType.COLUMN, "dept");
        Comparer dept_name_comp = new Comparer(ComparerType.COLUMN, "dept_name");
        Comparer thss_comp = new Comparer(ComparerType.STRING, "THSS");
        Comparer gpa_comp = new Comparer(ComparerType.COLUMN, "GPA");
        Comparer value_comp = new Comparer(ComparerType.NUMBER, "3.8");
        
        //null
        Condition condition1 = null;
        //name == "sgl"
        Condition condition2 = new Condition(name_comp, sgl_comp, ConditionType.EQ);
        //name >= 3.8
        Condition condition3 = new Condition(name_comp, value_comp, ConditionType.GE);
        
        //GPA > 3.8
        Condition condition4 = new Condition(gpa_comp, value_comp, ConditionType.GT);
        //name < 3.8
        Condition condition41 = new Condition(gpa_comp, value_comp, ConditionType.LT);
        //dept == "THSS"
        Condition condition5 = new Condition(dept_comp, thss_comp, ConditionType.EQ);
        //dept != "THSS"
        Condition condition6 = new Condition(dept_comp, thss_comp, ConditionType.NE);
        //student.id == grade.id
        Condition condition7 = new Condition(student_id_comp, grade_id_comp, ConditionType.EQ);
        //id == grade.id
        Condition condition8 = new Condition(id_comp, grade_id_comp, ConditionType.EQ);
        //dept == dept_name
        Condition condition9 = new Condition(dept_comp, dept_name_comp, ConditionType.EQ);
        
        //no logic
        Logic logic1 = new Logic(condition1);
        //"name == sgl"
        Logic logic2 = new Logic(condition2);
        //"name >= 3.8"
        Logic logic3 = new Logic(condition3);
        //GPA > 3.8
        Logic logic4 = new Logic(condition4);
        //GPA < 3.8
        Logic logic41 = new Logic(condition41);
        //name == "sgl" and dept == "THSS"
        Logic logic5 = new Logic(logic2, new Logic(condition5), LogicType.AND);
        //name == "sgl" or dept != "THSS"
        Logic logic6 = new Logic(logic2, new Logic(condition6), LogicType.OR);
        //name == "sgl" and dept != "THSS"
        Logic logic61 = new Logic(logic2, new Logic(condition6), LogicType.AND);
        //student.id == grade.id
        Logic logic7 = new Logic(condition7);
        //id == grade.id
        Logic logic8 = new Logic(condition8);
        //student.id == grade.id and GPA > 3.8
        Logic logic9 = new Logic(logic7, logic4, LogicType.AND);
        //student.id == grade.id and GPA < 3.8
        Logic logic91 = new Logic(logic7, logic41, LogicType.AND);
        //student.id == grade.id and dept == dept_name
        Logic logic10 = new Logic(logic7, new Logic(condition9), LogicType.AND);
        
        //only sgl---用于逻辑1,2,3,5,6
        JointRow joint1 = new JointRow(row1, table1);
        //only gpa---用于逻辑4
        JointRow joint2 = new JointRow(row4, table2);
        
        //sgl + gpa---用于逻辑7,8,9
        LinkedList<Row> list1 = new LinkedList<>();
        list1.push(row1);
        list1.push(row4);
        ArrayList<Table> list2 = new ArrayList<>();
        list2.add(table1);
        list2.add(table2);
        JointRow joint3 = new JointRow(list1, list2);
        
        //sgl + gpa +dept ---用于逻辑10
        LinkedList<Row> list3 = new LinkedList<>();
        list3.push(row1);
        list3.push(row4);
        list3.push(row7);
        ArrayList<Table> list4 = new ArrayList<>();
        list4.add(table1);
        list4.add(table2);
        list4.add(table3);
        JointRow joint4 = new JointRow(list3, list4);
        
        System.out.println("test1:" + logic1.GetResult(joint1)); //TRUE
        System.out.println("test2:" + logic2.GetResult(joint1)); //TRUE
        try {
            System.out.println("test3:" + logic3.GetResult(joint1)); //exception
        }
        catch (Exception e){
            System.out.println("test3:" + e.toString());
        }
        System.out.println("test4:" + logic4.GetResult(joint2)); //TRUE
        System.out.println("test4.1:" + logic41.GetResult(joint2)); //FALSE
        System.out.println("test5:" + logic5.GetResult(joint1)); //TRUE
        System.out.println("test6:" + logic6.GetResult(joint1)); //TRUE
        System.out.println("test6.1:" + logic61.GetResult(joint1)); //FALSE
        System.out.println("test7:" + logic7.GetResult(joint3)); //TRUE
        try {
            System.out.println("test8:" + logic8.GetResult(joint3)); //exception
        }
        catch (Exception e){
            System.out.println("test3:" + e.toString());
        }
        System.out.println("test9:" + logic9.GetResult(joint3)); //TRUE
        System.out.println("test9.1:" + logic91.GetResult(joint3)); //FALSE
        System.out.println("test10:" + logic10.GetResult(joint4));//TRUE
    
        manager.deleteDatabase("University");
    }
    
    /**
     *描述：测试select功能
     */
    /*@Test
    public void SelectTest()
    {
        Column student[] = {new Column("id", ColumnType.INT, 1, true, 10),
                new Column("name", ColumnType.STRING, 0, true, 10),
                new Column("dept", ColumnType.STRING, 0, true, 10),
                new Column("age", ColumnType.INT, 0, true, 10),
        };
        
        Column grade[] = {new Column("id", ColumnType.INT, 1, true, 10),
                new Column("GPA", ColumnType.DOUBLE, 0, true, 10),
                new Column("rank", ColumnType.INT, 0, true, 10),
        };
        
        Column department[] = {new Column("dept_name", ColumnType.STRING, 1, true, 10),
                new Column("involution", ColumnType.DOUBLE, 0, true, 10),
        };
        
        ArrayList<Column> column1 = new ArrayList<>(Arrays.asList(student));
        ArrayList<Column> column2 = new ArrayList<>(Arrays.asList(grade));
        ArrayList<Column> column3 = new ArrayList<>(Arrays.asList(department));
    
    
        manager.createDatabaseIfNotExists("University");
        Database university = manager.get("University");
        university.ToString();
        university.create("student", student);
        university.create("grade", grade);
        university.create("department", department);
        Table table1 = university.get("student");
        Table table2 = university.get("grade");
        Table table3 = university.get("department");
    
    
    
        Entry id1 = new Entry(1);
        Entry name1 = new Entry("sgl");
        Entry dept1 = new Entry("THSS");
        Entry age1 = new Entry(21);
        ArrayList<Entry> entries1 = new ArrayList<>();
        entries1.add(id1);
        entries1.add(name1);
        entries1.add(dept1);
        entries1.add(age1);
        table1.insert(column1, entries1);
        
        Entry id2 = new Entry(2);
        Entry name2 = new Entry("lsj");
        Entry dept2 = new Entry("THSS");
        Entry age2 = new Entry(20);
        ArrayList<Entry> entries2 = new ArrayList<>();
        entries2.add(id2);
        entries2.add(name2);
        entries2.add(dept2);
        entries2.add(age2);
        table1.insert(column1, entries2);
        
        Entry id3 = new Entry(3);
        Entry name3 = new Entry("borkball");
        Entry dept3 = new Entry("CST");
        Entry age3 = new Entry(21);
        ArrayList<Entry> entries3 = new ArrayList<>();
        entries3.add(id3);
        entries3.add(name3);
        entries3.add(dept3);
        entries3.add(age3);
        table1.insert(column1, entries3);
        
        
        Entry id4 = new Entry(1);
        Entry gpa4 = new Entry(3.81);
        Entry rank4 = new Entry(8);
        ArrayList<Entry> entries4 = new ArrayList<>();
        entries4.add(id4);
        entries4.add(gpa4);
        entries4.add(rank4);
        table2.insert(column2, entries4);
        
        Entry id5 = new Entry(2);
        Entry gpa5 = new Entry(3.71);
        Entry rank5 = new Entry(18);
        ArrayList<Entry> entries5 = new ArrayList<>();
        entries5.add(id5);
        entries5.add(gpa5);
        entries5.add(rank5);
        table2.insert(column2, entries5);
        
        Entry id6 = new Entry(3);
        Entry gpa6 = new Entry(3.61);
        Entry rank6 = new Entry(28);
        ArrayList<Entry> entries6 = new ArrayList<>();
        entries6.add(id6);
        entries6.add(gpa6);
        entries6.add(rank6);
        table2.insert(column2, entries6);
        
        Entry name7 = new Entry("THSS");
        Entry invo7 = new Entry(60.4);
        ArrayList<Entry> entries7 = new ArrayList<>();
        entries7.add(name7);
        entries7.add(invo7);
        table3.insert(column3, entries7);
        
        Entry name8 = new Entry("CST");
        Entry invo8 = new Entry(99.9);
        ArrayList<Entry> entries8 = new ArrayList<>();
        entries8.add(name8);
        entries8.add(invo8);
        table3.insert(column3, entries8);
        
        
        
        Comparer name_comp = new Comparer(ComparerType.COLUMN, "name");
        Comparer sgl_comp = new Comparer(ComparerType.STRING, "sgl");
        Comparer id_comp = new Comparer(ComparerType.COLUMN, "id");
        Comparer student_id_comp = new Comparer(ComparerType.COLUMN, "student.id");
        Comparer grade_id_comp = new Comparer(ComparerType.COLUMN, "grade.id");
        Comparer dept_comp = new Comparer(ComparerType.COLUMN, "dept");
        Comparer dept_name_comp = new Comparer(ComparerType.COLUMN, "dept_name");
        Comparer thss_comp = new Comparer(ComparerType.STRING, "THSS");
        Comparer gpa_comp = new Comparer(ComparerType.COLUMN, "GPA");
        Comparer value_comp = new Comparer(ComparerType.NUMBER, "3.8");
        Comparer id_value_comp = new Comparer(ComparerType.NUMBER, "1");
    
        //null
        Condition condition1 = null;
        //name == "sgl"
        Condition condition2 = new Condition(name_comp, sgl_comp, ConditionType.EQ);
        //name >= 3.8
        Condition condition3 = new Condition(name_comp, value_comp, ConditionType.GE);
        
        //GPA > 3.8
        Condition condition4 = new Condition(gpa_comp, value_comp, ConditionType.GT);
        //name < 3.8
        Condition condition41 = new Condition(gpa_comp, value_comp, ConditionType.LT);
        //dept == "THSS"
        Condition condition5 = new Condition(dept_comp, thss_comp, ConditionType.EQ);
        //dept != "THSS"
        Condition condition6 = new Condition(dept_comp, thss_comp, ConditionType.NE);
        //student.id == grade.id
        Condition condition7 = new Condition(student_id_comp, grade_id_comp, ConditionType.EQ);
        //id == grade.id
        Condition condition8 = new Condition(id_comp, grade_id_comp, ConditionType.EQ);
        //dept == dept_name
        Condition condition9 = new Condition(dept_comp, dept_name_comp, ConditionType.EQ);
        //id == 1
        Condition condition10 = new Condition(id_value_comp, id_comp, ConditionType.EQ);
        
        //no logic
        Logic logic1 = new Logic(condition1);
        //"name == sgl"
        Logic logic2 = new Logic(condition2);
        //"name >= 3.8"
        Logic logic3 = new Logic(condition3);
        //GPA > 3.8
        Logic logic4 = new Logic(condition4);
        //GPA < 3.8
        Logic logic41 = new Logic(condition41);
        //name == "sgl" and dept == "THSS"
        Logic logic5 = new Logic(logic2, new Logic(condition5), LogicType.AND);
        //name == "sgl" or dept != "THSS"
        Logic logic6 = new Logic(logic2, new Logic(condition6), LogicType.OR);
        //name == "sgl" and dept != "THSS"
        Logic logic61 = new Logic(logic2, new Logic(condition6), LogicType.AND);
        //student.id == grade.id
        Logic logic7 = new Logic(condition7);
        //id == grade.id
        Logic logic8 = new Logic(condition8);
        //student.id == grade.id and GPA > 3.8
        Logic logic9 = new Logic(logic7, logic4, LogicType.AND);
        //student.id == grade.id and GPA < 3.8
        Logic logic91 = new Logic(logic7, logic41, LogicType.AND);
        //student.id == grade.id and dept == dept_name
        Logic logic10 = new Logic(logic7, new Logic(condition9), LogicType.AND);
        //id == 1
        Logic logic11 = new Logic(condition10);
    
    
        QueryTable query0 = new SingleTable(table1);
        query0.SetLogicSelect(logic11);
        QueryTable query1 = new SingleTable(table1);
        query1.SetLogicSelect(logic1);
        QueryTable query2 = new SingleTable(table1);
        query2.SetLogicSelect(logic2);
        QueryTable query4 = new SingleTable(table2);
        query4.SetLogicSelect(logic4);
        QueryTable query41 = new SingleTable(table2);
        query41.SetLogicSelect(logic41);
        QueryTable query5 = new SingleTable(table1);
        query5.SetLogicSelect(logic5);
        QueryTable query6 = new SingleTable(table1);
        query6.SetLogicSelect(logic6);
        QueryTable query61 = new SingleTable(table1);
        query61.SetLogicSelect(logic61);
        
        ArrayList<Table> list1 = new ArrayList<>();
        list1.add(table1);
        list1.add(table2);
        QueryTable query7 = new JointTable(list1, logic7);
        query7.SetLogicSelect(logic1);
        QueryTable query71 = new JointTable(list1, logic7);
        query71.SetLogicSelect(logic2);

        QueryTable query74 = new JointTable(list1, logic7);
        query74.SetLogicSelect(logic4);
        QueryTable query741 = new JointTable(list1, logic7);
        query741.SetLogicSelect(logic41);
        QueryTable query75 = new JointTable(list1, logic7);
        query75.SetLogicSelect(logic5);
        
        ArrayList<Table> list2 = new ArrayList<>();
        list2.add(table1);
        list2.add(table2);
        list2.add(table3);
        QueryTable query10 = new JointTable(list2, logic10);
        query10.SetLogicSelect(logic1);
        QueryTable query102 = new JointTable(list2, logic10);
        query102.SetLogicSelect(logic2);

        QueryTable query104 = new JointTable(list2, logic10);
        query104.SetLogicSelect(logic4);
        QueryTable query1041 = new JointTable(list2, logic10);
        query1041.SetLogicSelect(logic41);
        QueryTable query105 = new JointTable(list2, logic10);
        query105.SetLogicSelect(logic5);
        
        String[] search1 = {"name", "student.id"};
        String[] search2 = {"name"};
        String[] search3 = null;
        String[] search4 = {"name", "id"};
    
    
        System.out.println("test 1:\n" + university.select(search3, query1, logic1, true));
        
        System.out.println("test 2:\n" + university.select(search1, query2, logic2, true));
    
    
        System.out.println("test 3:\n" + university.select(search3, query4, logic4, true));
        System.out.println("test 4:\n" + university.select(search3, query41, logic41, true));
        System.out.println("test 5:\n" + university.select(search3, query5, logic5, true));
        System.out.println("test 6:\n" + university.select(search3, query6, logic6, true));
        System.out.println("test 7:\n" + university.select(search3, query61, logic61, true));
        

        
        System.out.println("test 8:\n" + university.select(search3, query7, logic1, true));
        System.out.println("test 9:\n" + university.select(search1, query71, logic2, true));
        System.out.println("test 10:\n" + university.select(search3, query74, logic4, true));
        System.out.println("test 11:\n" + university.select(search1, query741, logic41, true));
        System.out.println("test 12:\n" + university.select(search3, query75, logic5, true));
    
        System.out.println("test 13:\n" + university.select(search3, query10, logic1, true));
        System.out.println("test 14:\n" + university.select(search3, query102, logic2, true));
        System.out.println("test 15:\n" + university.select(search3, query104, logic4, true));
        System.out.println("test 16:\n" + university.select(search3, query1041, logic41, true));
        System.out.println("test 17:\n" + university.select(search3, query105, logic5, true));
        System.out.println("test 18:\n" + university.select(search3, query0, logic11, true));
    
        manager.deleteDatabase("University");
    }*/
    
    /**
     *描述：测试insert功能
     */
    /*@Test
    public void InsertTest() {
        Column student[] = {new Column("id", ColumnType.INT, 1, true, 10),
                new Column("name", ColumnType.STRING, 0, true, 10),
                new Column("dept", ColumnType.STRING, 0, true, 10),
                new Column("age", ColumnType.INT, 0, false, 10),
        };
    
        Column grade[] = {new Column("id", ColumnType.INT, 1, true, 10),
                new Column("gpa", ColumnType.DOUBLE, 0, true, 10),
                new Column("rank", ColumnType.INT, 0, false, 10),
        };
    
        Column department[] = {new Column("dept_name", ColumnType.STRING, 1, true, 10),
                new Column("involution", ColumnType.DOUBLE, 0, true, 10),
        };
    
        ArrayList<Column> column1 = new ArrayList<>(Arrays.asList(student));
        ArrayList<Column> column2 = new ArrayList<>(Arrays.asList(grade));
        ArrayList<Column> column3 = new ArrayList<>(Arrays.asList(department));
    
    
        manager.createDatabaseIfNotExists("University");
        Database university = manager.get("University");
        university.create("student", student);
        university.create("grade", grade);
        university.create("department", department);
        Table table1 = university.get("student");
        Table table2 = university.get("grade");
        Table table3 = university.get("department");
        
        //正常
        String[] test_column_1 = {"name", "id", "dept" , "age"};
        String[] test_value_1 = {"'sgl'", "1", "'THSS'", "21"};
    
        //notnull 测试1
        String[] test_column_2 = {"name", "id", "dept" , "age"};
        String[] test_value_2 = {"'lsj'", "2", "'THSS'", "null"};
    
        //notnull 测试2
        String[] test_column_3 = {"name", "id", "dept" , "age"};
        String[] test_value_3 = {"'lsj'", "3", "null", "21"};
        
        //列不匹配测试1
        String[] test_column_4 = {"name", "id", "dept" };
        String[] test_value_4 = {"'sgl'", "4", "'THSS'"};
    
        //列不匹配测试2
        String[] test_column_41 = {"name", "id", "age" };
        String[] test_value_41 = {"'sgl'", "4", "21"};
        
        //列不匹配测试2
        String[] test_column_5 = {"name", "id", "dept" , "age"};
        String[] test_value_5 = {"5", "’lsj‘", "'THSS'", "20"};
        
        //Max length 测试1
        String[] test_column_6 = {"name", "id", "dept" , "age"};
        String[] test_value_6 = {"'ceddin deden neslin baban'", "6", "'THSS'", "21"};
    
        //Max length 测试2
        String[] test_column_7 = {"name", "id", "dept" , "age"};
        String[] test_value_7 = {"'1234567890'", "7", "'THSS'", "21"};
    
        //重复测试
        String[] test_column_8 = {"name", "id", "dept" , "age"};
        String[] test_value_8 = {"'lsj'", "1", "'CST'", "18"};
    
        //列不匹配测试--int改成double
        String[] test_column_9 = {"name", "id", "dept" , "age"};
        String[] test_value_9 = {"'sgl'", "10", "'THSS'", "20.1"};
    
        //列不匹配测试--double 改成int
        String[] test_column_10 = {"rank", "gpa", "id"};
        String[] test_value_10 = {"1", "4", "4"};
        
        //没有列测试1
        String[] test_value_11 = {"11", "'kebab'", "'SPQR'", "1453"};
        //没有列测试2
        String[] test_value_12 = {"12", "'kebab'", "'SPQR'"};
        //没有列测试3
        String[] test_value_13 = {"13", "'kebab'", "1453", "'SPQR'"};
        //没有列测试3
        String[] test_value_14 = {"14", "'kebab'"};
        //没有列测试1
        String[] test_value_15 = {"15", "'kebab'", "'SPQR'", "1453","de"};
        
        try {
            university.insert("student", test_column_1, test_value_1);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query1 = new SingleTable(table1);
        System.out.println("test 1:" + university.select(null, query1, null, true));
        try {
            university.insert("student", test_column_2, test_value_2);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query2 = new SingleTable(table1);
        System.out.println("test 2:" + university.select(null, query2, null, true));
        try {
            university.insert("student", test_column_3, test_value_3);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query3 = new SingleTable(table1);
        System.out.println("test 3:" + university.select(null, query3, null, true));
        try {
            university.insert("student", test_column_4, test_value_4);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query4 = new SingleTable(table1);
        System.out.println("test 4:" + university.select(null, query4, null, true));
        try {
            university.insert("student", test_column_41, test_value_41);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query41 = new SingleTable(table1);
        System.out.println("test 41:" + university.select(null, query41, null, true));
        try {
            university.insert("student", test_column_5, test_value_5);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query5 = new SingleTable(table1);
        System.out.println("test 5:" + university.select(null, query5, null, true));
        try {
            university.insert("student", test_column_6, test_value_6);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query6 = new SingleTable(table1);
        System.out.println("test 6:" + university.select(null, query6, null, true));
        try {
            university.insert("student", test_column_7, test_value_7);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query7 = new SingleTable(table1);
        System.out.println("test 7:" + university.select(null, query7, null, true));
        try {
            university.insert("student", test_column_8, test_value_8);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query8 = new SingleTable(table1);
        System.out.println("test 8:" + university.select(null, query8, null, true));
    
        try {
            university.insert("student", test_column_9, test_value_9);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query9 = new SingleTable(table1);
        System.out.println("test 9:" + university.select(null, query9, null, true));
    
        try {
            university.insert("grade", test_column_10, test_value_10);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query10 = new SingleTable(table2);
        System.out.println("test 10:" + university.select(null, query10, null, true));
    
        try {
            university.insert("student", null, test_value_11);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query11 = new SingleTable(table1);
        System.out.println("test 11:" + university.select(null, query11, null, true));
    
    
        try {
            university.insert("student", null, test_value_12);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query12 = new SingleTable(table1);
        System.out.println("test 12:" + university.select(null, query12, null, true));
    
        try {
            university.insert("student", null, test_value_13);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query13 = new SingleTable(table1);
        System.out.println("test 13:" + university.select(null, query13, null, true));
    
        try {
            university.insert("student", null, test_value_14);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query14 = new SingleTable(table1);
        System.out.println("test 15:" + university.select(null, query14, null, true));
    
        try {
            university.insert("student", null, test_value_15);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        QueryTable query15 = new SingleTable(table1);
        System.out.println("test 15:" + university.select(null, query15, null, true));
    
        manager.deleteDatabase("University");
    }*/
    
    /**
     *描述：测试delete功能
     */
    /*@Test
    public void DeleteTest() {
        Column student[] = {new Column("id", ColumnType.INT, 1, true, 10),
                new Column("name", ColumnType.STRING, 0, true, 10),
                new Column("dept", ColumnType.STRING, 0, true, 10),
                new Column("age", ColumnType.INT, 0, false, 10),
        };
    
        Column grade[] = {new Column("id", ColumnType.INT, 1, true, 10),
                new Column("GPA", ColumnType.DOUBLE, 0, true, 10),
                new Column("rank", ColumnType.INT, 0, false, 10),
        };
    
        Column department[] = {new Column("dept_name", ColumnType.STRING, 1, true, 10),
                new Column("involution", ColumnType.DOUBLE, 0, true, 10),
        };
    
        ArrayList<Column> column1 = new ArrayList<>(Arrays.asList(student));
        ArrayList<Column> column2 = new ArrayList<>(Arrays.asList(grade));
        ArrayList<Column> column3 = new ArrayList<>(Arrays.asList(department));
    
    
        manager.createDatabaseIfNotExists("University");
        Database university = manager.get("University");
        university.create("student", student);
        university.create("grade", grade);
        university.create("department", department);
        Table table1 = university.get("student");
        Table table2 = university.get("grade");
        Table table3 = university.get("department");
    
        String[] column_1 = {"name", "id", "dept" , "age"};
        String[] value_1 = {"'sgl'", "1", "'THSS'", "21"};
    
        String[] column_2 = {"name", "id", "dept" , "age"};
        String[] value_2 = {"'tql'", "2", "'THSS'", "null"};
    
        String[] column_3 = {"name", "id", "dept" , "age"};
        String[] value_3 = {"'borkball'", "3", "'CST'", "21"};
    
        String[] column_4 = {"id", "GPA", "rank"};
        String[] value_4 = {"1", "3.81", "8"};
    
        String[] column_5 = {"id", "GPA", "rank"};
        String[] value_5 = {"2", "3.71", "18"};
    
        String[] column_6 = {"id", "GPA", "rank"};
        String[] value_6 = {"3", "3.61", "28"};
    
        String[] column_7 = {"dept_name", "involution"};
        String[] value_7 = {"'THSS'", "60.4"};
        
        String[] column_8 = {"dept_name", "involution"};
        String[] value_8 = {"'cst'", "99.4"};
        
    
        Comparer name_comp = new Comparer(ComparerType.COLUMN, "name");
        Comparer sgl_comp = new Comparer(ComparerType.STRING, "sgl");
        Comparer id_comp = new Comparer(ComparerType.COLUMN, "id");
        Comparer value_comp = new Comparer(ComparerType.NUMBER, "2");
        Comparer student_id_comp = new Comparer(ComparerType.COLUMN, "student.id");
        Comparer dept_comp = new Comparer(ComparerType.COLUMN, "dept");
        Comparer thss_comp = new Comparer(ComparerType.STRING, "THSS");

    
        //null
        Condition condition1 = null;
        //name == "sgl"
        Condition condition2 = new Condition(name_comp, sgl_comp, ConditionType.EQ);
        //name > sgl
        Condition condition3 = new Condition(name_comp, sgl_comp, ConditionType.GT);
        //name >= 3.8
        Condition condition4 = new Condition(name_comp, value_comp, ConditionType.GE);
        //dept == "THSS"
        Condition condition5 = new Condition(dept_comp, thss_comp, ConditionType.EQ);
        //dept != "THSS"
        Condition condition6 = new Condition(dept_comp, thss_comp, ConditionType.NE);
        //id == 2
        Condition condition7 = new Condition(id_comp, value_comp, ConditionType.EQ);
        //student_id > 2
        Condition condition8 = new Condition(student_id_comp, value_comp, ConditionType.GT);
        //id == 1
        Condition condition9 = new Condition(id_comp, new Comparer(ComparerType.NUMBER, "1"), ConditionType.EQ);
        
        //no logic
        Logic logic1 = new Logic(condition1); //sgl tql borkball
        //"name == sgl"
        Logic logic2 = new Logic(condition2); //sgl
        //"name > sgl"
        Logic logic3 = new Logic(condition3); //tql
        //name > 3.8
        Logic logic4 = new Logic(condition4); //error
        //dept == "THSS"
        Logic logic5 = new Logic(condition5); //sgl tql
        //id == 2
        Logic logic6 = new Logic(condition7); //tql
        //student.id > 2
        Logic logic7 = new Logic(condition8); //borkball
        //name == "sgl" and dept != "THSS" //空
        Logic logic8 = new Logic(logic2, new Logic(condition6), LogicType.AND);
    
        //name > "sgl" or id > 2 //tql,borkball
        Logic logic9 = new Logic(logic3, logic7, LogicType.OR);
    
        //name == "sgl" and dept == "THSS" and id == 1//sgl
        Logic logic10 = new Logic(logic2, new Logic(logic5, new Logic(condition9), LogicType.AND), LogicType.AND);
    
        //name == sgl or dept != "THSS" or name > sgl //全
        Logic logic11 = new Logic(logic2, new Logic(new Logic(condition6), logic3, LogicType.OR), LogicType.OR);
    
        //name == "sgl" or (dept == "THSS" and id == 2) //sgl,tql
        Logic logic12 = new Logic(logic2, new Logic(logic5, logic6, LogicType.AND), LogicType.OR);
    
        ArrayList<Logic> logiclist = new ArrayList<>();
        logiclist.add(logic1);
        logiclist.add(logic2);
        logiclist.add(logic3);
        logiclist.add(logic4);
        logiclist.add(logic5);
        logiclist.add(logic6);
        logiclist.add(logic7);
        logiclist.add(logic8);
        logiclist.add(logic9);
        logiclist.add(logic10);
        logiclist.add(logic11);
        logiclist.add(logic12);
        int i = 0;
        for(Logic logic:logiclist) {
            try {
                i ++;
                university.insert("student", column_1, value_1);
                university.insert("student", column_2, value_2);
                university.insert("student", column_3, value_3);
                QueryTable query1 = new SingleTable(table1);
                System.out.println("test " + i + ": "  + university.select(null, query1, logic, true));
                System.out.println("test " + i + ": "  + university.delete("student", logic));
                QueryTable query2 = new SingleTable(table1);
                System.out.println("test " + i + ": "  + university.select(null, query2, null, true));
                university.delete("student", logic1);
            } catch (Exception e) {
                System.out.println("test " + i + ": "  + e.toString());
                university.delete("student", logic1);
            }
        }
        manager.deleteDatabase("University");
    }*/
    
    /**
     *描述：测试update功能
     */
    /*
    @Test
    public void UpdateTest() {
        Column student[] = {new Column("id", ColumnType.INT, 1, true, 10),
                new Column("name", ColumnType.STRING, 0, true, 10),
                new Column("dept", ColumnType.STRING, 0, true, 10),
                new Column("age", ColumnType.INT, 0, false, 10),
        };
    
        Column grade[] = {new Column("id", ColumnType.INT, 1, true, 10),
                new Column("gpa", ColumnType.DOUBLE, 0, true, 10),
                new Column("rank", ColumnType.INT, 0, false, 10),
        };
    
        Column department[] = {new Column("dept_name", ColumnType.STRING, 1, true, 10),
                new Column("involution", ColumnType.DOUBLE, 0, true, 10),
        };
    
        ArrayList<Column> column1 = new ArrayList<>(Arrays.asList(student));
        ArrayList<Column> column2 = new ArrayList<>(Arrays.asList(grade));
        ArrayList<Column> column3 = new ArrayList<>(Arrays.asList(department));
    
    
        manager.createDatabaseIfNotExists("University");
        Database university = manager.get("University");
        university.create("student", student);
        university.create("grade", grade);
        university.create("department", department);
        Table table1 = university.get("student");
        Table table2 = university.get("grade");
        Table table3 = university.get("department");
    
        String[] column_1 = {"name", "id", "dept" , "age"};
        String[] value_1 = {"'sgl'", "1", "'THSS'", "21"};
    
        String[] column_2 = {"name", "id", "dept" , "age"};
        String[] value_2 = {"'tql'", "2", "'THSS'", "null"};
    
        String[] column_3 = {"name", "id", "dept" , "age"};
        String[] value_3 = {"'borkball'", "3", "'CST'", "21"};
    
        String[] column_4 = {"id", "gpa", "rank"};
        String[] value_4 = {"1", "3.81", "8"};
    
        String[] column_5 = {"id", "gpa", "rank"};
        String[] value_5 = {"2", "3.71", "18"};
    
        String[] column_6 = {"id", "gpa", "rank"};
        String[] value_6 = {"3", "3.61", "28"};
    
        String[] column_7 = {"dept_name", "involution"};
        String[] value_7 = {"'THSS'", "60.4"};
    
        String[] column_8 = {"dept_name", "involution"};
        String[] value_8 = {"'cst'", "99.4"};
        

    
        Comparer name_comp = new Comparer(ComparerType.COLUMN, "name");
        Comparer sgl_comp = new Comparer(ComparerType.STRING, "sgl");
        Comparer id_comp = new Comparer(ComparerType.COLUMN, "id");
        Comparer value_comp = new Comparer(ComparerType.NUMBER, "2");
        Comparer student_id_comp = new Comparer(ComparerType.COLUMN, "student.id");
        Comparer dept_comp = new Comparer(ComparerType.COLUMN, "dept");
        Comparer thss_comp = new Comparer(ComparerType.STRING, "THSS");
    
    
        //null
        Condition condition1 = null;
        //name == "sgl"
        Condition condition2 = new Condition(name_comp, sgl_comp, ConditionType.EQ);
        //name > sgl
        Condition condition3 = new Condition(name_comp, sgl_comp, ConditionType.GT);
        //name >= 3.8
        Condition condition4 = new Condition(name_comp, value_comp, ConditionType.GE);
        //dept == "THSS"
        Condition condition5 = new Condition(dept_comp, thss_comp, ConditionType.EQ);
        //dept != "THSS"
        Condition condition6 = new Condition(dept_comp, thss_comp, ConditionType.NE);
        //id == 2
        Condition condition7 = new Condition(id_comp, value_comp, ConditionType.EQ);
        //student_id > 2
        Condition condition8 = new Condition(student_id_comp, value_comp, ConditionType.GT);
        //id == 1
        Condition condition9 = new Condition(id_comp, new Comparer(ComparerType.NUMBER, "1"), ConditionType.EQ);
    
        //no logic
        Logic logic3 = new Logic(condition1); //sgl tql borkball
        //"name == sgl"
        Logic logic1 = new Logic(condition2); //sgl
        //dept == "THSS"
        Logic logic2 = new Logic(condition5); //sgl tql

        //name == "sgl" and dept != "THSS" //空
        Logic logic0 = new Logic(logic2, new Logic(condition6), LogicType.AND);
        
        ArrayList<Logic> logiclist = new ArrayList<>();
        logiclist.add(logic0);
        logiclist.add(logic1);
        logiclist.add(logic2);
        logiclist.add(logic3);

        //正常更新
        String name1 = "name";
        Comparer value1 = new Comparer(ComparerType.STRING, "lsj");
        //更新导致主键重复
        String name2 = "id";
        Comparer value2 = new Comparer(ComparerType.NUMBER, "4");
        //更新类型不匹配---number改string
        String name3 = "age";
        Comparer value3 = new Comparer(ComparerType.STRING, "sgl");
        //更新类型不匹配---string改number
        String name4 = "name";
        Comparer value4 = new Comparer(ComparerType.NUMBER, "1");
        //更新类型匹配---int改double
        String name5 = "age";
        Comparer value5 = new Comparer(ComparerType.NUMBER, "22.1");
        //更新类型匹配---double改int
        String name6 = "gpa";
        Comparer value6 = new Comparer(ComparerType.NUMBER, "4");
        //更新导致可null变成null
        String name7 = "age";
        Comparer value7 = new Comparer(ComparerType.NULL, "ceddin deden, neslin baban!");
        //更新导致不可null变成null
        String name8 = "name";
        Comparer value8 = new Comparer(ComparerType.NULL, "ceddin deden, neslin baban!");
        //更新导致超过max length
        String name9 = "name";
        Comparer value9 = new Comparer(ComparerType.STRING, "ceddin deden, neslin baban!");
        //更新导致没超过max length
        String name10 = "name";
        Comparer value10 = new Comparer(ComparerType.STRING, "1234567890");
        //更新把别的改成column
        String name11 = "name";
        Comparer value11 = new Comparer(ComparerType.COLUMN, "name");
        //更新把别的改成column
        String name12 = "age";
        Comparer value12 = new Comparer(ComparerType.COLUMN, "21");
        String[] name_list = {name1, name2, name3, name4, name5, name6, name7, name8, name9, name10, name11, name12};
        Comparer[] value_list = {value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11, value12};
        
        for(int i = 1; i <= name_list.length; i ++) {
            String name = name_list[i - 1];
            Comparer value = value_list[i - 1];
            
            
            if(i == 6) {
                try {
                    university.insert("grade", column_4, value_4);
                    university.insert("grade", column_5, value_5);
                    university.insert("grade", column_6, value_6);
                    String a = university.update("grade", name, value, logic3);
                    System.out.println("test " + i + ": " + a);
                    QueryTable query = new SingleTable(table2);
                    
                    System.out.println("test " + i + ": " + university.select(null, query, null, true));
                    university.delete("grade", logic3);
                } catch (Exception e) {
                    System.out.println("test " + i + ": " + e.toString());
                    university.delete("grade", logic3);
                }
            }
            else {
                for (Logic logic : logiclist) {
                    try {
                        university.insert("student", column_1, value_1);
                        university.insert("student", column_2, value_2);
                        university.insert("student", column_3, value_3);
                        String a = university.update("student", name, value, logic);
                        System.out.println("test " + i + ": " + a);
                        QueryTable query = new SingleTable(table1);
                        System.out.println("test " + i + ": " + university.select(null, query, null, true));
                        university.delete("student", logic3);
                    } catch (Exception e) {
                        System.out.println("test " + i + ": " + e.toString());
                        university.delete("student", logic3);
                    }
                }
            }
        }
        manager.deleteDatabase("University");
    }*/
    

}

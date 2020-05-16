package cn.edu.thssdb.query;

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
            //System.out.println("test3:" + logic3.GetResult(joint1)); //exception
            System.out.println("test4:" + logic4.GetResult(joint2)); //TRUE
            System.out.println("test4.1:" + logic41.GetResult(joint2)); //FALSE
            System.out.println("test5:" + logic5.GetResult(joint1)); //TRUE
            System.out.println("test6:" + logic6.GetResult(joint1)); //TRUE
            System.out.println("test6.1:" + logic61.GetResult(joint1)); //FALSE
            System.out.println("test7:" + logic7.GetResult(joint3)); //TRUE
            //System.out.println("test8:" + logic8.GetResult(joint3)); //exception
            System.out.println("test9:" + logic9.GetResult(joint3)); //TRUE
            System.out.println("test9.1:" + logic91.GetResult(joint3)); //FALSE
            System.out.println("test10:" + logic10.GetResult(joint4));//TRUE
        }
    
    @Test
    public void SelectTestRaw()
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
        
        /*
        //ArrayList<Row> answer11 = university.select(search1, query1, logic1, true);
        //ArrayList<Row> answer12 = university.select(search2, query1, logic1, true);
        ArrayList<Row> answer13 = university.select(search3, query1, logic1, true);
        //ArrayList<Row> answer14 = university.select(search4, query1, logic1, true);
        
        ArrayList<Row> answer21 = university.select(search1, query2, logic2, true);
        //ArrayList<Row> answer22 = university.select(search2, query2, logic2, true);
        //ArrayList<Row> answer23 = university.select(search3, query2, logic2, true);
        //ArrayList<Row> answer24 = university.select(search4, query2, logic2, true);
    
        ArrayList<Row> answer4 = university.select(search3, query4, logic4, true);
        ArrayList<Row> answer41 = university.select(search3, query41, logic41, true);
        ArrayList<Row> answer5 = university.select(search3, query5, logic5, true);
        ArrayList<Row> answer6 = university.select(search3, query6, logic6, true);
        ArrayList<Row> answer61 = university.select(search3, query61, logic61, true);
        */

        
        //ArrayList<Row> answer71 = university.select(search1, query7, logic1, true);
        ArrayList<Row> answer73 = university.select(search3, query7, logic1, true);
        ArrayList<Row> answer711 = university.select(search1, query71, logic2, true);
        //ArrayList<Row> answer713 = university.select(search3, query71, logic2, true);
        ArrayList<Row> answer740 = university.select(search3, query74, logic4, true);
        ArrayList<Row> answer741 = university.select(search1, query741, logic41, true);
        ArrayList<Row> answer75 = university.select(search3, query75, logic5, true);
    
        ArrayList<Row> answer10 = university.select(search3, query10, logic1, true);
        ArrayList<Row> answer102 = university.select(search3, query102, logic2, true);
        ArrayList<Row> answer104 = university.select(search3, query104, logic4, true);
        ArrayList<Row> answer1041 = university.select(search3, query1041, logic41, true);
        ArrayList<Row> answer105 = university.select(search3, query105, logic5, true);
        
    }
}

package cn.edu.thssdb.query;

import cn.edu.thssdb.parser.SQLHandler;
import cn.edu.thssdb.schema.Manager;
import org.junit.BeforeClass;
import org.junit.Test;

public class SQLTest {
	public static SQLHandler handler;
	public static Manager manager;
	
	@BeforeClass
	public static void setUp() {
		manager = Manager.getInstance();
		handler = new SQLHandler(manager);
	}
	/**
	 * 描述：测试sql语句（数据库建立，删除，切换，输出；表建立，删除，输出，简单查询，增删改）
	 */
	@Test
	public void MainTest() {
		
		System.out.println(handler.evaluate("create database university"));
		System.out.println(handler.evaluate("show database university"));
		System.out.println(handler.evaluate("create table student (name string(10) not null, id int, dept string(10) not null, age int, primary key(id))"));
		System.out.println(handler.evaluate("show table student"));
		System.out.println(handler.evaluate("show database university"));
		
		//测试新增
		String insert1 = "INSERT INTO student(id, age, name, dept) VALUES (1, 21, 'sgl', 'THSS')";
		String insert2 = "INSERT INTO student(id, age, name, dept) VALUES (2, null, 'sj', 'THSS')";
		String insert3 = "INSERT INTO student(id, age, name, dept) VALUES (3, 21, 'borkball', 'CST')";
		String insert4 = "INSERT INTO student(id, age, name) VALUES (4, 20, 'lsj')";
		String insert5 = "INSERT INTO student(id, dept, name) VALUES (4, 'THSS', 'lsj')";
		String insert6 = "INSERT INTO student VALUES ('kebab', 5, 'CST', 1453)";
		String insert7 = "INSERT INTO student VALUES ('kebab', 6, 'CST')";
		String insert8 = "INSERT INTO student VALUES ('kebab', 7)";
		String insert9 = "INSERT INTO student VALUES ('kebab', 7, 'CST')";
		
		
		String select_all = "Select * from student";
		
		System.out.println(handler.evaluate(insert1));
		System.out.println(handler.evaluate(insert2));
		System.out.println(handler.evaluate(insert3));
		System.out.println(handler.evaluate(insert4));
		System.out.println(handler.evaluate(insert5));
		System.out.println(handler.evaluate(insert6));
		System.out.println(handler.evaluate(insert7));
		System.out.println(handler.evaluate(insert8));
		System.out.println(handler.evaluate(insert9));
		System.out.println(handler.evaluate(select_all));
		
		//测试删除
		String delete1 = "Delete from student where id = 7";
		String delete2 = "Delete from student where age='kebab'";
		String delete3 = "Delete from student where name='kebab'";
		String delete4 = "Delete from student where id>10";
		String delete5 = "Delete from student where name = 'sgl' || name < 'sgl' && dept = 'THSS' ";
		String delete6 = "Delete from student";
		
		System.out.println(handler.evaluate(delete1));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(delete2));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(delete3));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(delete4));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(delete5));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(delete6));
		System.out.println(handler.evaluate(select_all));
		
		System.out.println(handler.evaluate(insert1));
		System.out.println(handler.evaluate(insert2));
		System.out.println(handler.evaluate(insert3));
		System.out.println(handler.evaluate(insert4));
		System.out.println(handler.evaluate(insert5));
		System.out.println(handler.evaluate(insert6));
		System.out.println(handler.evaluate(insert7));
		System.out.println(handler.evaluate(insert8));
		System.out.println(handler.evaluate(insert9));
		System.out.println(handler.evaluate(select_all));
		
		//测试更新
		String update1 = "update student set name = 'lsj' where name = 'sgl'";
		String update2 = "update student set name = 10 where name = 'lsj'";
		String update3 = "update student set name = null where name = 'lsj'";
		String update4 = "update student set name = 'sgl' where name = 'lsj'";
		String update5 = "update student set age = 'kebab' where name = 'sgl'";
		String update6 = "update student set age = 20 where name = 'sgl'";
		String update7 = "update student set age = 21.4 where name = 'sgl'";
		String update8 = "update student set age = null where name = 'sgl'";
		String update9 = "update student set id = 2 where name = 'sgl'";
		String update10 = "update student set name = 'lsj' where name = 'sgl' || name < 'sgl' && dept = 'CST'";
		
		System.out.println(handler.evaluate(update1));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(update2));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(update3));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(update4));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(update5));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(update6));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(update7));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(update8));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(update9));
		System.out.println(handler.evaluate(select_all));
		System.out.println(handler.evaluate(update10));
		System.out.println(handler.evaluate(select_all));
		
		//测试droptable
		System.out.println(handler.evaluate("drop table student"));
		System.out.println(handler.evaluate("show database university"));
		System.out.println(handler.evaluate("show table student"));
		System.out.println(handler.evaluate("select * from student"));
		System.out.println(handler.evaluate("create table student (name string(10) not null, id int, dept string(10) not null, age int, primary key(id))"));
		System.out.println(handler.evaluate("show database university"));
		System.out.println(handler.evaluate("show table student"));
		//测试切换数据库
		System.out.println(handler.evaluate("create database turkey"));
		System.out.println(handler.evaluate("show table student"));
		System.out.println(handler.evaluate("use turkey"));
		System.out.println(handler.evaluate("show table student"));
		System.out.println(handler.evaluate("create table student (name string(10) not null, id int, dept string(10) not null, age int, primary key(id))"));
		System.out.println(handler.evaluate("show table student"));
		System.out.println(handler.evaluate("select * from student"));
		
		//测试删除数据库
		System.out.println(handler.evaluate("drop database university"));
		System.out.println(handler.evaluate("show database turkey"));
		System.out.println(handler.evaluate("drop database turkey"));
		
	}
	
	/**
	 * 描述：测试sql语句（数据库建立，删除，切换，输出；表建立，删除，输出，简单查询，增删改）
	 */
	@Test
	public void SearchTest() {
	
	}
}

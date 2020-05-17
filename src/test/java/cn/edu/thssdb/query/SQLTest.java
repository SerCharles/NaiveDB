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
	 * 描述：测试sql语句
	 */
	@Test
	public void InsertTest() {
		
		System.out.println(handler.evaluate("create database university"));
		System.out.println(handler.evaluate("show database university"));
		System.out.println(handler.evaluate("create table student (name string(10) not null, id int, dept string(10) not null, age int, primary key(id))"));
		System.out.println(handler.evaluate("show table student"));
		System.out.println(handler.evaluate("show database university"));
		
		String insert1 = "INSERT INTO student(id, age, name, dept) VALUES (1, 21, 'sgl', 'THSS')";
		String insert2 = "INSERT INTO student(id, age, name, dept) VALUES (2, null, 'sj', 'THSS')";
		String insert3 = "INSERT INTO student(id, age, name, dept) VALUES (3, 21, 'borkball', 'CST')";
		String insert4 = "INSERT INTO student(id, age, name) VALUES (4, 20, 'lsj')";
		String insert5 = "INSERT INTO student(id, dept, name) VALUES (4, 'THSS', 'lsj')";
		String insert6 = "INSERT INTO student VALUES ('kebab', 5, 'CST', 1453)";
		String insert7 = "INSERT INTO student VALUES ('kebab', 6, 'CST')";
		String insert8 = "INSERT INTO student VALUES ('kebab', 7)";
		
		
		String select_all = "Select * from student";
		
		System.out.println(handler.evaluate(insert1));
		System.out.println(handler.evaluate(insert2));
		System.out.println(handler.evaluate(insert3));
		System.out.println(handler.evaluate(insert4));
		System.out.println(handler.evaluate(insert5));
		System.out.println(handler.evaluate(insert6));
		System.out.println(handler.evaluate(insert7));
		
		System.out.println(handler.evaluate(select_all));
		
		System.out.println(handler.evaluate("drop database university"));
	}
}

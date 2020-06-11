package cn.edu.thssdb.client;

import cn.edu.thssdb.rpc.thrift.ConnectReq;
import cn.edu.thssdb.rpc.thrift.ConnectResp;
import cn.edu.thssdb.rpc.thrift.DisconnectReq;
import cn.edu.thssdb.rpc.thrift.DisconnectResp;
import cn.edu.thssdb.rpc.thrift.ExecuteStatementReq;
import cn.edu.thssdb.rpc.thrift.ExecuteStatementResp;
import cn.edu.thssdb.rpc.thrift.IService;
import cn.edu.thssdb.utils.Global;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientTest {

  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  private static final PrintStream SCREEN_PRINTER = new PrintStream(System.out);

  private static TTransport transport;
  private static TProtocol protocol;
  private static IService.Client client;

  public static void main(String[] args) {
    try {
      transport = new TSocket(Global.DEFAULT_SERVER_HOST, Global.DEFAULT_SERVER_PORT);
      transport.open();
      protocol = new TBinaryProtocol(transport);
      client = new IService.Client(protocol);

      long sessionId = connect();
      TestExample(sessionId);
      TestBasic(sessionId);
      TestQuery(sessionId);
      disconnect(sessionId);

      transport.close();
    } catch (TException e) {
      logger.error(e.getMessage());
    }
  }
  
  private static long TestBasic(long sessionId) throws TException {
    {
      String statement_build =
              "create database university;" +
                      "use university;" +
                      "create table student (name string(10) not null, id int, dept string(10) not null, age int, primary key(id))";
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement_build);
      ExecuteStatementResp resp = client.executeStatement(req);
      for (String item : resp.columnsList) {
        println(item);
      }
    }
  
    String statement_insert =
            "INSERT INTO student(id, age, name, dept) VALUES (1, 21, 'sgl', 'THSS');" +
                    "INSERT INTO student(id, age, name, dept) VALUES (2, null, 'sj', 'THSS');" +
                    "INSERT INTO student(id, age, name, dept) VALUES (3, 21, 'borkball', 'CST');"+
                    "INSERT INTO student(id, age, name) VALUES (4, 20, 'lsj');"+
                    "INSERT INTO student(id, dept, name) VALUES (4, 'THSS', 'lsj');"+
                    "INSERT INTO student VALUES ('kebab', 5, 'CST', 1453);"+
                    "INSERT INTO student VALUES ('kebab', 6, 'CST');"+
                    "INSERT INTO student VALUES ('kebab', 7);"+
                    "INSERT INTO student VALUES ('kebab', 7, 'CST');";
    //测试新增
    {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement_insert);
      ExecuteStatementResp resp = client.executeStatement(req);
      for (String item : resp.columnsList) {
        println(item);
      }
    }
    
    String select_all = "Select id, age, name, dept from student";
    String delete_all = "Delete from student;";
  
    List<String> column_result = new ArrayList<>(Arrays.asList("id", "age", "name", "dept"));
    List<List<String>> row_result_insert = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "21", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "lsj", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("7", "null", "kebab", "CST"))
            ));
    {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, select_all);
      ExecuteStatementResp resp = client.executeStatement(req);
      if (resp.getColumnsList().equals(column_result) && resp.getRowList().equals(row_result_insert)) {
        println("Insert is correct!");
      }
    }
    
  
    //测试删除
    String delete1 = "Delete from student where id = 7;";
    String delete2 = "Delete from student where age='kebab';";
    String delete3 = "Delete from student where name='kebab';";
    String delete4 = "Delete from student where id>10;";
    String delete5 = "Delete from student where name = 'sgl' or name < 'sgl' and dept = 'THSS';";
    
    List<List<String>> row_result_delete_1 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "21", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "lsj", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "kebab", "CST"))
    ));
  
    List<List<String>> row_result_delete_2 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "21", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "lsj", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "kebab", "CST"))
            ));
  
    List<List<String>> row_result_delete_3 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "21", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "lsj", "THSS"))
            ));
  
    List<List<String>> row_result_delete_4 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "21", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "lsj", "THSS"))
    ));
  
    List<List<String>> row_result_delete_5 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST"))
    ));
  
    ArrayList<String> statement_delete = new ArrayList<>();
    statement_delete.add(delete1);
    statement_delete.add(delete2);
    statement_delete.add(delete3);
    statement_delete.add(delete4);
    statement_delete.add(delete5);
  
    ArrayList<List<List<String>>> row_result_delete = new ArrayList<>();
    row_result_delete.add(row_result_delete_1);
    row_result_delete.add(row_result_delete_2);
    row_result_delete.add(row_result_delete_3);
    row_result_delete.add(row_result_delete_4);
    row_result_delete.add(row_result_delete_5);
    
    for(int i = 0; i < row_result_delete.size(); i ++) {
      {
        ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement_delete.get(i));
        ExecuteStatementResp resp = client.executeStatement(req);
        println(resp.getColumnsList().get(0));
      }
  
      {
        ExecuteStatementReq req = new ExecuteStatementReq(sessionId, select_all);
        ExecuteStatementResp resp = client.executeStatement(req);
        if (resp.getColumnsList().equals(column_result) && resp.getRowList().equals(row_result_delete.get(i))) {
          println("Delete " + (i + 1) + " is correct!");
        }
      }
    }
  
    {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, delete_all);
      ExecuteStatementResp resp = client.executeStatement(req);
      println(resp.getColumnsList().get(0));
    }
  
    {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement_insert);
      ExecuteStatementResp resp = client.executeStatement(req);
      for (String item : resp.columnsList) {
        println(item);
      }
    }

    String update1 = "update student set name = 'lsj' where name = 'sgl'";
    String update2 = "update student set name = 10 where name = 'lsj'";
    String update3 = "update student set name = null where name = 'lsj'";
    String update4 = "update student set name = 'sgl' where name = 'lsj'";
    String update5 = "update student set age = 'kebab' where name = 'sgl'";
    String update6 = "update student set age = 20 where name = 'sgl'";
    String update7 = "update student set age = 21.4 where name = 'sgl'";
    String update8 = "update student set age = null where name = 'sgl'";
    String update9 = "update student set id = 2 where name = 'sgl'";
    String update10 = "update student set name = 'lsj' where name = 'sgl' or name < 'sgl' and dept = 'CST'";
    ArrayList<String> statement_update = new ArrayList<>();
    statement_update.add(update1);
    statement_update.add(update2);
    statement_update.add(update3);
    statement_update.add(update4);
    statement_update.add(update5);
    statement_update.add(update6);
    statement_update.add(update7);
    statement_update.add(update8);
    statement_update.add(update9);
    statement_update.add(update10);
  
    List<List<String>> row_result_update_1 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "21", "lsj", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "lsj", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("7", "null", "kebab", "CST"))
    ));
  
    List<List<String>> row_result_update_2 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "21", "lsj", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "lsj", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("7", "null", "kebab", "CST"))
    ));
  
    List<List<String>> row_result_update_3 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "21", "lsj", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "lsj", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("7", "null", "kebab", "CST"))
    ));
  
    List<List<String>> row_result_update_4 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "21", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("7", "null", "kebab", "CST"))
    ));
  
    List<List<String>> row_result_update_5 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "21", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("7", "null", "kebab", "CST"))
    ));
  
    List<List<String>> row_result_update_6 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "20", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "20", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("7", "null", "kebab", "CST"))
    ));
  
    List<List<String>> row_result_update_7 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "21", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "21", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("7", "null", "kebab", "CST"))
    ));
  
    List<List<String>> row_result_update_8 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "null", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("7", "null", "kebab", "CST"))
    ));
  
    List<List<String>> row_result_update_9 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "null", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "borkball", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "sgl", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "kebab", "CST")),
            new ArrayList<>(Arrays.asList("7", "null", "kebab", "CST"))
    ));
  
    List<List<String>> row_result_update_10 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("1", "null", "lsj", "THSS")),
            new ArrayList<>(Arrays.asList("2", "null", "sj", "THSS")),
            new ArrayList<>(Arrays.asList("3", "21", "lsj", "CST")),
            new ArrayList<>(Arrays.asList("4", "null", "lsj", "THSS")),
            new ArrayList<>(Arrays.asList("5", "1453", "lsj", "CST")),
            new ArrayList<>(Arrays.asList("6", "null", "lsj", "CST")),
            new ArrayList<>(Arrays.asList("7", "null", "lsj", "CST"))
    ));
  
    ArrayList<List<List<String>>> row_result_update = new ArrayList<>();
    row_result_update.add(row_result_update_1);
    row_result_update.add(row_result_update_2);
    row_result_update.add(row_result_update_3);
    row_result_update.add(row_result_update_4);
    row_result_update.add(row_result_update_5);
    row_result_update.add(row_result_update_6);
    row_result_update.add(row_result_update_7);
    row_result_update.add(row_result_update_8);
    row_result_update.add(row_result_update_9);
    row_result_update.add(row_result_update_10);
  
    for(int i = 0; i < row_result_update.size(); i ++) {
      {
        ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement_update.get(i));
        ExecuteStatementResp resp = client.executeStatement(req);
        println(resp.getColumnsList().get(0));
      }
    
      {
        ExecuteStatementReq req = new ExecuteStatementReq(sessionId, select_all);
        ExecuteStatementResp resp = client.executeStatement(req);
        if (resp.getColumnsList().equals(column_result) && resp.getRowList().equals(row_result_update.get(i))) {
          println("Update " + (i + 1) + " is correct!");
        }
      }
    }
  
    {
      String statement_drop =
              "delete from student;"+
              "drop table student;"+
              "drop database university;";
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement_drop);
      ExecuteStatementResp resp = client.executeStatement(req);
      for (String item : resp.columnsList) {
        println(item);
      }
    }
    
    return sessionId;
  }
  
  private static long TestQuery(long sessionId) throws TException {
  
    String[] statements_build = {
            "create database university;",
            "use university;",
            "create table student (name string(10) not null, id int, dept string(10) not null, age int, primary key(id));",
            "create table grade (id int, gpa double not null, rank int, primary key(id))",
            "create table department (dept_name string(10), involution double)",
            "create table department (dept_name string(10), involution double, primary key(dept_name))"};
    for (String statement : statements_build) {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      println(resp.columnsList.get(0));
    }

  
    String[] statements_insert = {
            "insert into student values ('sgl',1,'THSS',22);",
            "insert into student values ('sj',2,'THSS');",
            "insert into student values ('borkball',3,'CST',21);",
            "insert into grade values (1, 3.81, 8);",
            "insert into grade values (2, 3.6);",
            "insert into grade values (3, 3.71, 28);",
            "insert into department values ('THSS');",
            "insert into department values ('CST', 99.999);"};
    for (String statement : statements_insert) {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      println(resp.columnsList.get(0));
    }
    
    
    //单表无条件
    String select1 = "select name, id, dept, age from student";
    List<String> column_result_1 = new ArrayList<>(Arrays.asList("name", "id", "dept", "age"));
    List<List<String>> row_result_1 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("sgl", "1", "THSS", "22")),
            new ArrayList<>(Arrays.asList("sj", "2", "THSS", "null")),
            new ArrayList<>(Arrays.asList("borkball", "3", "CST", "21"))
    ));
    
    String select2 = "select dept, student.name from student";
    List<String> column_result_2 = new ArrayList<>(Arrays.asList("dept", "student.name"));
    List<List<String>> row_result_2 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("THSS", "sgl")),
            new ArrayList<>(Arrays.asList("THSS", "sj")),
            new ArrayList<>(Arrays.asList("CST", "borkball"))
    ));
    
    
    //不合法列
    String select3 = "select kebab from student";
    List<String> column_result_3 = new ArrayList<>(Arrays.asList());
    List<List<String>> row_result_3 = new ArrayList<>(Arrays.asList(
    ));
    
    String select4 = "select kebab.name from student";
    List<String> column_result_4 = new ArrayList<>(Arrays.asList());
    List<List<String>> row_result_4 = new ArrayList<>(Arrays.asList(
    ));
    
    //单表单一条件
    String select5 = "select name, id, dept, age from student where name = 'sgl'";
    List<String> column_result_5 = new ArrayList<>(Arrays.asList("name", "id", "dept", "age"));
    List<List<String>> row_result_5 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("sgl", "1", "THSS", "22"))
    ));
    
    String select6 = "select dept, student.name from student where name < 22";
    List<String> column_result_6 = new ArrayList<>(Arrays.asList());
    List<List<String>> row_result_6 = new ArrayList<>(Arrays.asList(
    ));
    
    
    //单表复合条件
    String select7 = "select name, id, dept, age from student where name = 'sgl' or name > 'sgl' and 'THSS' = dept";
    List<String> column_result_7 = new ArrayList<>(Arrays.asList("name", "id", "dept", "age"));
    List<List<String>> row_result_7 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("sgl", "1", "THSS", "22")),
            new ArrayList<>(Arrays.asList("sj", "2", "THSS", "null"))
    ));
    
    String select8 = "select dept, student.name from student where student.age < 22 and 3 < 4.1 and id < age";
    List<String> column_result_8 = new ArrayList<>(Arrays.asList("dept", "student.name"));
    List<List<String>> row_result_8 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("CST", "borkball"))
    ));
    
    String select9 = "select dept, student.name from student where student.age < 22";
    List<String> column_result_9 = new ArrayList<>(Arrays.asList("dept", "student.name"));
    List<List<String>> row_result_9 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("CST", "borkball"))
    ));
    
    String select10 = "select dept, student.name from student where age > id";
    List<String> column_result_10 = new ArrayList<>(Arrays.asList("dept", "student.name"));
    List<List<String>> row_result_10 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("THSS", "sgl")),
            new ArrayList<>(Arrays.asList("CST", "borkball"))
    ));
  
    
    //条件不合法
    String select11 = "select name, id, dept, age from student where name = 21";
    List<String> column_result_11 = new ArrayList<>(Arrays.asList());
    List<List<String>> row_result_11 = new ArrayList<>(Arrays.asList(
    ));
    
    String select12 = "select name, id, dept, age from student where 1 = 2";
    List<String> column_result_12 = new ArrayList<>(Arrays.asList("name", "id", "dept", "age"));
    List<List<String>> row_result_12 = new ArrayList<>(Arrays.asList());
    
    String select13 = "select name, id, dept, age from student where kebab = 21";
    List<String> column_result_13 = new ArrayList<>(Arrays.asList());
    List<List<String>> row_result_13 = new ArrayList<>(Arrays.asList(
    ));
    
    String select14 = "select name, id, dept, age from student where student.kebab = 21";
    List<String> column_result_14 = new ArrayList<>(Arrays.asList());
    List<List<String>> row_result_14 = new ArrayList<>(Arrays.asList(
    ));
  
    //两表join
    String select15 = "select name, student.id, dept, age, gpa, grade.rank from student join grade on student.id = grade.id";
    List<String> column_result_15 = new ArrayList<>(Arrays.asList("name", "student.id", "dept", "age", "gpa", "grade.rank"));
    List<List<String>> row_result_15 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("sgl", "1", "THSS", "22", "3.81", "8")),
            new ArrayList<>(Arrays.asList("sj", "2", "THSS", "null", "3.6", "null")),
            new ArrayList<>(Arrays.asList("borkball", "3", "CST", "21", "3.71", "28"))
    ));
  
    String select16 = "select name, gpa from student join grade on student.id = grade.id where gpa > 3.7";
    List<String> column_result_16 = new ArrayList<>(Arrays.asList("name", "gpa"));
    List<List<String>> row_result_16 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("sgl", "3.81")),
            new ArrayList<>(Arrays.asList("borkball", "3.71"))
    ));
    
    String select17 = "select name, gpa from student join grade on student.id = grade.id where gpa < 3.7 or rank < 10";
    List<String> column_result_17 = new ArrayList<>(Arrays.asList("name", "gpa"));
    List<List<String>> row_result_17 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("sgl", "3.81")),
            new ArrayList<>(Arrays.asList("sj", "3.6"))
    ));
  
  
    String select18 = "select name, gpa from student join grade on student.id = grade.id where id = 1";
    List<String> column_result_18 = new ArrayList<>();
    List<List<String>> row_result_18 = new ArrayList<>(Arrays.asList(
    ));
  
    //三表join
    String select19 = "select name, student.id, dept, age, gpa, grade.rank, involution from student join grade join department on student.id = grade.id and dept_name = dept";
    List<String> column_result_19 = new ArrayList<>(Arrays.asList("name", "student.id", "dept", "age", "gpa", "grade.rank", "involution"));
    List<List<String>> row_result_19 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("sgl", "1", "THSS", "22", "3.81", "8", "null")),
            new ArrayList<>(Arrays.asList("sj", "2", "THSS", "null", "3.6", "null", "null")),
            new ArrayList<>(Arrays.asList("borkball", "3", "CST", "21", "3.71", "28", "99.999"))
    ));
    
    
    String select20 = "select department.dept_name from student join grade join department on student.id = grade.id and dept_name = dept";
    List<String> column_result_20 = new ArrayList<>(Arrays.asList("department.dept_name"));
    List<List<String>> row_result_20 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("THSS")),
            new ArrayList<>(Arrays.asList("THSS")),
            new ArrayList<>(Arrays.asList("CST"))
    ));
    String select21 = "select distinct department.dept_name from student join grade join department on student.id = grade.id and dept_name = dept";
    List<String> column_result_21 = new ArrayList<>(Arrays.asList("department.dept_name"));
    List<List<String>> row_result_21 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("THSS")),
            new ArrayList<>(Arrays.asList("CST"))
    ));
    
    String select22 = "select name, student.id, dept, age, gpa, grade.rank, department.involution from student join grade join department on student.id = grade.id and dept_name = dept where gpa > 3.8 or name = 'sj'";
    List<String> column_result_22 = new ArrayList<>(Arrays.asList("name", "student.id", "dept", "age", "gpa", "grade.rank", "department.involution"));
    List<List<String>> row_result_22 = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("sgl", "1", "THSS", "22", "3.81", "8", "null")),
            new ArrayList<>(Arrays.asList("sj", "2", "THSS", "null", "3.6", "null", "null"))
    ));
    
    ArrayList<String> statements = new ArrayList<>();
    statements.add(select1);
    statements.add(select2);
    statements.add(select3);
    statements.add(select4);
    statements.add(select5);
    statements.add(select6);
    statements.add(select7);
    statements.add(select8);
    statements.add(select9);
    statements.add(select10);
    statements.add(select11);
    statements.add(select12);
    statements.add(select13);
    statements.add(select14);
    statements.add(select15);
    statements.add(select16);
    statements.add(select17);
    statements.add(select18);
    statements.add(select19);
    statements.add(select20);
    statements.add(select21);
    statements.add(select22);
    
    
    ArrayList<List<String>> result_column_list = new ArrayList<>();
    result_column_list.add(column_result_1);
    result_column_list.add(column_result_2);
    result_column_list.add(column_result_3);
    result_column_list.add(column_result_4);
    result_column_list.add(column_result_5);
    result_column_list.add(column_result_6);
    result_column_list.add(column_result_7);
    result_column_list.add(column_result_8);
    result_column_list.add(column_result_9);
    result_column_list.add(column_result_10);
    result_column_list.add(column_result_11);
    result_column_list.add(column_result_12);
    result_column_list.add(column_result_13);
    result_column_list.add(column_result_14);
    result_column_list.add(column_result_15);
    result_column_list.add(column_result_16);
    result_column_list.add(column_result_17);
    result_column_list.add(column_result_18);
    result_column_list.add(column_result_19);
    result_column_list.add(column_result_20);
    result_column_list.add(column_result_21);
    result_column_list.add(column_result_22);
  
    ArrayList<List<List<String>>> result_row_list = new ArrayList<>();
    result_row_list.add(row_result_1);
    result_row_list.add(row_result_2);
    result_row_list.add(row_result_3);
    result_row_list.add(row_result_4);
    result_row_list.add(row_result_5);
    result_row_list.add(row_result_6);
    result_row_list.add(row_result_7);
    result_row_list.add(row_result_8);
    result_row_list.add(row_result_9);
    result_row_list.add(row_result_10);
    result_row_list.add(row_result_11);
    result_row_list.add(row_result_12);
    result_row_list.add(row_result_13);
    result_row_list.add(row_result_14);
    result_row_list.add(row_result_15);
    result_row_list.add(row_result_16);
    result_row_list.add(row_result_17);
    result_row_list.add(row_result_18);
    result_row_list.add(row_result_19);
    result_row_list.add(row_result_20);
    result_row_list.add(row_result_21);
    result_row_list.add(row_result_22);
  
    for(int i = 0; i < result_row_list.size(); i ++) {
      int index = i + 1;
      {
        ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statements.get(i));
        ExecuteStatementResp resp = client.executeStatement(req);
        if(index == 3 || index == 4 || index == 6 || index == 11 || index == 13 || index == 14 || index == 18) {
          if(resp.getColumnsList().size() == 1) {
            println(resp.getColumnsList().get(0));
            println("Query " + index + " is correct!");
          }
        }
        else if (resp.getColumnsList().equals(result_column_list.get(i)) && resp.getRowList().equals(result_row_list.get(i))) {
          println("Query " + index + " is correct!");
        }
      }
    }
  
    String[] statements_drop = {
            "delete from student;",
            "delete from grade",
            "delete from department",
            "drop table grade",
            "drop table department",
            "drop table student;",
            "drop database university;"};
    for (String statement : statements_drop) {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      println(resp.columnsList.get(0));
    }
    return sessionId;
  }
  
  private static void TestExample(long sessionId) throws TException {
    {
      String statement = "\n  create database test;   use test;\ncreate table person \n(name String(256), ID Int not null, primary key(ID));";
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      if (resp.getStatus().code == Global.SUCCESS_CODE) {
        println("Init Successfully!");
      }
    }

    {
      long startTime = System.currentTimeMillis();
      String statement = "insert into person values ('Anna', 20);" +
              "\n  insert into person values ('Bob', 22);" +
              "\n  insert into person values ('Cindy', 30);";
        ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
        ExecuteStatementResp resp = client.executeStatement(req);
        if (resp.getStatus().code == Global.SUCCESS_CODE) {
          println("Insert Data Successfully!");
        }
      println("It costs " + (System.currentTimeMillis() - startTime) + "ms.");
    }
    
    {
      long startTime = System.currentTimeMillis();
      String statement = "select name, ID from person;";
      List<String> columnsList = new ArrayList<>(Arrays.asList("name", "id"));
      List<List<String>> rowList = new ArrayList<>(Arrays.asList(
              new ArrayList<>(Arrays.asList("Anna", "20")),
              new ArrayList<>(Arrays.asList("Bob", "22")),
              new ArrayList<>(Arrays.asList("Cindy", "30"))
      ));
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      if (resp.getStatus().code == Global.SUCCESS_CODE) {
        println("Query Data Successfully!");
      }
      println("It costs " + (System.currentTimeMillis() - startTime) + "ms.");
      if (resp.getColumnsList().equals(columnsList) && resp.getRowList().equals(rowList)) {
        println("The Result Set is Correct!");
      }
    }
  
    {
      String statement = "delete from person;drop table person;drop database test;";
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      if (resp.getStatus().code == Global.SUCCESS_CODE) {
        println("Delete Data Successfully!");
      }
    }
    
  }
  
  private static long connect() throws TException {
    String username = "username";
    String password = "password";
    ConnectReq req = new ConnectReq(username, password);
    ConnectResp resp = client.connect(req);
    if (resp.getStatus().code == Global.SUCCESS_CODE) {
      println("Connect Successfully!");
    }
    return resp.getSessionId();
  }

  private static void disconnect(long sessionId) throws TException {
    DisconnectReq req = new DisconnectReq(sessionId);
    DisconnectResp resp = client.disconnect(req);
    if (resp.getStatus().code == Global.SUCCESS_CODE) {
      println("Disconnect Successfully!");
    }
  }

  static void println(String msg) {
    SCREEN_PRINTER.println(msg);
  }
}

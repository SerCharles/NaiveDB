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
      createDatabase(sessionId);
      useDatabase(sessionId);
      TestBasic(sessionId);
      //createTable(sessionId);
      //insertData(sessionId);
      //queryData(sessionId);
      disconnect(sessionId);

      transport.close();
    } catch (TException e) {
      logger.error(e.getMessage());
    }
  }
  
  private static long TestBasic(long sessionId) throws TException {
  
    String[] statements_build = {
            "create database university;",
            "use university;",
            "create table student (name string(10) not null, id int, dept string(10) not null, age int, primary key(id));"};
    for (String statement : statements_build) {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      println(resp.columnsList.get(0));
    }
    
    //测试新增
    String[] statements_insert = {
            "INSERT INTO student(id, age, name, dept) VALUES (1, 21, 'sgl', 'THSS');",
            "INSERT INTO student(id, age, name, dept) VALUES (2, null, 'sj', 'THSS');",
            "INSERT INTO student(id, age, name, dept) VALUES (3, 21, 'borkball', 'CST');",
            "INSERT INTO student(id, age, name) VALUES (4, 20, 'lsj');",
            "INSERT INTO student(id, dept, name) VALUES (4, 'THSS', 'lsj');",
            "INSERT INTO student VALUES ('kebab', 5, 'CST', 1453);",
            "INSERT INTO student VALUES ('kebab', 6, 'CST');",
            "INSERT INTO student VALUES ('kebab', 7);",
            "INSERT INTO student VALUES ('kebab', 7, 'CST');"};
    for (String statement : statements_insert) {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      println(resp.columnsList.get(0));
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
  
    for (String statement : statements_insert) {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      println(resp.columnsList.get(0));
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
  
    String[] statements_drop = {
            "delete from student;",
            "drop table student;",
            "drop database university;"};
    for (String statement : statements_drop) {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      println(resp.columnsList.get(0));
    }
    
    return sessionId;
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

  private static void createDatabase(long sessionId) throws TException {
    String statement = "create database test;";
    ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
    ExecuteStatementResp resp = client.executeStatement(req);
    if (resp.getStatus().code == Global.SUCCESS_CODE) {
      println("Create Database Successfully!");
    }
  }

  private static void useDatabase(long sessionId) throws TException {
    String statement = "use test;";
    ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
    ExecuteStatementResp resp = client.executeStatement(req);
    if (resp.getStatus().code == Global.SUCCESS_CODE) {
      println("Use Database Successfully!");
    }
  }

  private static void createTable(long sessionId) throws TException {
    String statement = "create table person (name String(256), ID Int not null, primary key(ID));";
    ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
    ExecuteStatementResp resp = client.executeStatement(req);
    if (resp.getStatus().code == Global.SUCCESS_CODE) {
      println("Create Table Successfully!");
    }
  }

  private static void insertData(long sessionId) throws TException {
    long startTime = System.currentTimeMillis();
    String[] statements = {"insert into person values ('Anna', 20);",
        "insert into person values ('Bob', 22);",
        "insert into person values ('Cindy', 30);"};
    for (String statement : statements) {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      if (resp.getStatus().code == Global.SUCCESS_CODE) {
        println("Insert Data Successfully!");
      }
    }
    println("It costs " + (System.currentTimeMillis() - startTime) + "ms.");
  }

  private static void queryData(long sessionId) throws TException {
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

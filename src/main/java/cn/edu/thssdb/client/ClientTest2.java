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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ClientTest2 {

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

      List<String> insertStatements = loadInsertStatements();

      long sessionId = connect();
      createDatabase(sessionId);
      useDatabase(sessionId);
      createTable(sessionId);
      insertData(sessionId, insertStatements);
      queryData(sessionId);
      disconnect(sessionId);

      transport.close();
    } catch (TException | IOException e) {
      logger.error(e.getMessage());
    }
  }

  private static List<String> loadInsertStatements() throws IOException {
    List<String> statements = new ArrayList<>();
    File file = new File("insert_into.sql");
    if (file.exists() && file.isFile()){
      FileInputStream fileInputStream = new FileInputStream(file);
      InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        statements.add(line);
      }
      bufferedReader.close();
      inputStreamReader.close();
      fileInputStream.close();
    }
    return statements;
  }

  private static long connect() throws TException {
    String username = "username";
    String password = "password";
    ConnectReq req = new ConnectReq(username, password);
    ConnectResp resp = client.connect(req);
    if (resp.getStatus().code == Global.SUCCESS_CODE) {
      println("Connect Successfully!");
    } else {
      println("Connect Unsuccessfully!");
    }
    return resp.getSessionId();
  }

  private static void createDatabase(long sessionId) throws TException {
    String statement = "create database test;";
    ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
    ExecuteStatementResp resp = client.executeStatement(req);
    if (resp.getStatus().code == Global.SUCCESS_CODE) {
      println("Create Database Successfully!");
    } else {
      println("Create Database Unsuccessfully!");
    }
  }

  private static void useDatabase(long sessionId) throws TException {
    String statement = "use test;";
    ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
    ExecuteStatementResp resp = client.executeStatement(req);
    if (resp.getStatus().code == Global.SUCCESS_CODE) {
      println("Use Database Successfully!");
    } else {
      println("Use Database Unsuccessfully!");
    }
  }

  private static void createTable(long sessionId) throws TException {
    String[] statements = {
        "create table department (dept_name String(20), building String(15), budget Double, primary key(dept_name));",
        "create table course (course_id String(8), title String(50), dept_name String(20), credits Int, primary key(course_id));",
        "create table instructor (i_id String(5), i_name String(20) not null, dept_name String(20), salary Float, primary key(i_id));",
        "create table student (s_id String(5), s_name String(20) not null, dept_name String(20), tot_cred Int, primary key(s_id));",
        "create table advisor (s_id String(5), i_id String(5), primary key (s_id));"
    };
    for (String statement : statements) {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      if (resp.getStatus().code == Global.SUCCESS_CODE) {
        println("Create Table Successfully!");
      } else {
        println("Create Table Unsuccessfully!");
      }
    }
  }

  private static void insertData(long sessionId, List<String> statements) throws TException {
    long startTime = System.currentTimeMillis();
    boolean success = true;
    for (String statement : statements) {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statement);
      ExecuteStatementResp resp = client.executeStatement(req);
      if (resp.getStatus().code == Global.FAILURE_CODE) {
        success = false;
      }
    }
    if (success) {
      println("Insert Data Successfully!");
    } else {
      println("Insert Data Unsuccessfully!");
    }
    println("It costs " + (System.currentTimeMillis() - startTime) + "ms.");
  }

  private static void queryData(long sessionId) throws TException {
    long startTime = System.currentTimeMillis();
    String[] statements = {
        "select s_id, s_name, dept_name, tot_cred from student;",
        "select course_id, title from course where credits >= 4;",
        "select s_id, s_name from student where dept_name = 'Physics';",
        "select course_id, title from course join department on course.dept_name = department.dept_name where building <> 'Palmer';",
        "select s_id from instructor join advisor on instructor.i_id = advisor.i_id where i_name = 'Luo';"
    };
    int[] results = {2000, 92, 96, 182, 44};
    for (int i = 0; i < statements.length; i++) {
      ExecuteStatementReq req = new ExecuteStatementReq(sessionId, statements[i]);
      ExecuteStatementResp resp = client.executeStatement(req);
      if (resp.getStatus().code == Global.SUCCESS_CODE) {
        println("Query Data Successfully!");
      } else {
        println("Query Data Unsuccessfully!");
      }
      if (resp.getRowList().size() == results[i]) {
        println("The Result Set is Correct!");
      } else {
        println("The Result Set is Wrong!");
      }
    }
    println("It costs " + (System.currentTimeMillis() - startTime) + "ms.");
  }

  private static void disconnect(long sessionId) throws TException {
    DisconnectReq req = new DisconnectReq(sessionId);
    DisconnectResp resp = client.disconnect(req);
    if (resp.getStatus().code == Global.SUCCESS_CODE) {
      println("Disconnect Successfully!");
    } else {
      println("Disconnect Unsuccessfully!");
    }
  }

  static void println(String msg) {
    SCREEN_PRINTER.println(msg);
  }
}

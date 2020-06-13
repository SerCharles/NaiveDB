package cn.edu.thssdb.service;

import cn.edu.thssdb.parser.SQLHandler;
import cn.edu.thssdb.query.QueryResult;
import cn.edu.thssdb.rpc.thrift.*;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.utils.Global;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IServiceHandler implements IService.Iface {
  public static SQLHandler handler;
  public static Manager manager;
  private long currentSession = 0;
  
  public IServiceHandler() {
    super();
    manager = Manager.getInstance();
    handler = new SQLHandler(manager);
  }
  @Override
  public GetTimeResp getTime(GetTimeReq req) throws TException {
    GetTimeResp resp = new GetTimeResp();
    resp.setTime(new Date().toString());
    resp.setStatus(new Status(Global.SUCCESS_CODE));
    return resp;
  }

  @Override
  /**
   * 描述：连接，找到一个可用的session返回给客户端，如果没有就返回
   */
  public ConnectResp connect(ConnectReq req) throws TException {
    System.out.println("username: " + req.username + "\npassword: " + req.password);
    //分配新的session
    long new_session = currentSession;
    currentSession ++;
    ConnectResp resp = new ConnectResp();
  
    if(new_session < 0) {
      Status the_status = new Status(Global.FAILURE_CODE);
      the_status.setMsg("Cannot find available session!");
      resp.setStatus(the_status);
    }
    resp.setStatus(new Status(Global.SUCCESS_CODE));
    resp.setSessionId(new_session);
    return resp;
  }
  

  @Override
  /**
   * 描述：断开连接
   */
  public DisconnectResp disconnect(DisconnectReq req) throws TException {
    DisconnectResp resp = new DisconnectResp();
    resp.setStatus(new Status(Global.SUCCESS_CODE));
    return resp;
  }
  

  @Override
  /**
   * 描述：执行指令
   * 只有已经连接的客户端才能执行指令
   * 当前就是读取一行然后扔进查询模块执行，然后结果返回回去
   */
  public ExecuteStatementResp executeStatement(ExecuteStatementReq req) throws TException {
  
    ExecuteStatementResp the_response = new ExecuteStatementResp();
    long the_session = req.getSessionId();
    if(the_session < 0 || the_session >= currentSession) {
      Status the_status = new Status(Global.FAILURE_CODE);
      the_status.setMsg("Not connected yet!");
      the_response.setStatus(the_status);
      return the_response;
    }

    String command_full = req.statement;
    if(command_full == null || command_full.isEmpty()) {
      the_response.setStatus(new Status(Global.SUCCESS_CODE));
      the_response.addToColumnsList("Null instruction!");
      return the_response;
    }
    
    String[] commands = command_full.split(";");
    ArrayList<QueryResult> result = new ArrayList<>();
  
    for(String command : commands) {
      command = command.trim();
      if(command.length() == 0) {
        continue;
      }
      String cmd = command.split("\\s+")[0];
      ArrayList<QueryResult> the_result;
      if ((cmd.toLowerCase().equals("insert") || cmd.toLowerCase().equals("update") || cmd.toLowerCase().equals("delete") || cmd.toLowerCase().equals("select")) && !manager.transaction_sessions.contains(the_session)) {
        handler.evaluate("autobegin transaction", the_session);
        the_result = handler.evaluate(command, the_session);
        result.addAll(the_result);
        handler.evaluate("autocommit", the_session);
    
      } else {
        the_result = handler.evaluate(command, the_session);
        result.addAll(the_result);
      }
    }
    the_response.setStatus(new Status(Global.SUCCESS_CODE));
  
    if(result == null) {
      the_response.addToColumnsList("null");
    }
    //有且仅有一个正确查询
    else if(result.size() == 1 && result.get(0) != null && result.get(0).mWhetherRight == true) {
      for(Row row : result.get(0).mResultList) {
        ArrayList<String> the_result = row.toStringList();
        the_response.addToRowList(the_result);
      }
      if(the_response.isSetRowList() == false) {
        the_response.rowList = new ArrayList<>();
      }
      for(String column_name: result.get(0).mColumnName) {
        the_response.addToColumnsList(column_name);
      }
    }
    
    //1-多条非查询/错误
    else {
      for(QueryResult item : result) {
        if(item == null) {
          the_response.addToColumnsList("null");
        }
        else {
          the_response.addToColumnsList(item.mErrorMessage);
        }
      }
    }
    
    return the_response;
  }
}

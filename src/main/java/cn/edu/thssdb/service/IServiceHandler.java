package cn.edu.thssdb.service;

import cn.edu.thssdb.parser.SQLHandler;
import cn.edu.thssdb.rpc.thrift.*;
import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.utils.Global;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.Date;

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
    //resp.setStatus(new Status(Global.SUCCESS_CODE));
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

    String command = req.statement.toLowerCase();
    String cmd = command.split("\\s+")[0];
    String result;
    if((cmd.equals("insert") || cmd.equals("update") || cmd.equals("delete") || cmd.equals("select")) && !manager.transaction_sessions.contains(the_session))
    {
      handler.evaluate("autobegin transaction", the_session);
      result = handler.evaluate(command, the_session);
      handler.evaluate("autocommit", the_session);

    }else
    {
      result = handler.evaluate(command, the_session);
    }

    ArrayList<String> the_result = new ArrayList<>();
    the_result.add(result);
    
    the_response.setStatus(new Status(Global.SUCCESS_CODE));
    the_response.addToRowList(the_result);
    return the_response;
  }
}

package cn.edu.thssdb.client;

import cn.edu.thssdb.rpc.thrift.*;
import cn.edu.thssdb.utils.Global;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Scanner;

public class Client {

  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  static final String HOST_ARGS = "h";
  static final String HOST_NAME = "host";

  static final String HELP_ARGS = "help";
  static final String HELP_NAME = "help";

  static final String PORT_ARGS = "p";
  static final String PORT_NAME = "port";

  private static final PrintStream SCREEN_PRINTER = new PrintStream(System.out);
  private static final Scanner SCANNER = new Scanner(System.in);

  private static TTransport transport;
  private static TProtocol protocol;
  private static IService.Client client;
  private static CommandLine commandLine;
  
  //TODO 这个类一堆static我觉得有问题，不能支持多个客户端，但是不知道具体咋整
  private static long session = -1;
  
  public static void main(String[] args) {
    commandLine = parseCmd(args);
    if (commandLine.hasOption(HELP_ARGS)) {
      showHelp();
      return;
    }
    try {
      echoStarting();
      String host = commandLine.getOptionValue(HOST_ARGS, Global.DEFAULT_SERVER_HOST);
      int port = Integer.parseInt(commandLine.getOptionValue(PORT_ARGS, String.valueOf(Global.DEFAULT_SERVER_PORT)));
      transport = new TSocket(host, port);
      transport.open();
      protocol = new TBinaryProtocol(transport);
      client = new IService.Client(protocol);
      boolean open = true;
      while (true) {
        print(Global.CLI_PREFIX);
        String msg = SCANNER.nextLine();
        long startTime = System.currentTimeMillis();
        switch (msg.trim()) {
          case Global.SHOW_TIME:
            getTime();
            break;
          case Global.QUIT:
            open = false;
            break;
          case Global.CONNECT:
            connect();
            break;
          case Global.DISCONNECT:
            disconnect();
            break;
          default:
            executeStatement(msg.trim());
            break;
        }
        long endTime = System.currentTimeMillis();
        println("It costs " + (endTime - startTime) + " ms.");
        if (!open) {
          break;
        }
      }
      transport.close();
    } catch (TTransportException e) {
      logger.error(e.getMessage());
    }
  }

  private static void getTime() {
    GetTimeReq req = new GetTimeReq();
    try {
      println(client.getTime(req).getTime());
    } catch (TException e) {
      logger.error(e.getMessage());
    }
  }
  
  /**
  描述：连接服务器，其实就是获取一个可用session，session<0是没有session，>=0是有
   */
  private static void connect() {
    if(session >= 0)  {
      println("Already connected!");
      return;
    }
    ConnectReq the_request = new ConnectReq(Global.USERNAME, Global.PASSWORD);
    try {
      ConnectResp the_respond = client.connect(the_request);
      println(the_respond.toString());
      if(the_respond.getStatus().code == Global.SUCCESS_CODE) {
        session = the_respond.getSessionId();
      }
    } catch (TException e) {
      logger.error(e.getMessage());
    }
  }
  
  /**
   描述：断开服务器，session设置-1
   */
  private static void disconnect() {
    if(session < 0)  {
      println("Not connected yet!");
      return;
    }
    DisconnetResp the_request = new DisconnetResp();
    the_request.setStatus(new Status(Global.SUCCESS_CODE));
    try {
      DisconnetResp the_respond = client.disconnect(the_request);
      println(the_respond.toString());
      if(the_respond.getStatus().code == Global.SUCCESS_CODE) {
        session = -1;
      }
    } catch (TException e) {
      logger.error(e.getMessage());
    }
  }
  
  /**
   描述：执行语句，显示结果
   */
  private static void executeStatement(String message) {
    if(session < 0)  {
      println("Not connected yet!");
      return;
    }
    ExecuteStatementReq the_request = new ExecuteStatementReq();
    the_request.setStatement(message);
    the_request.setSessionId(session);
    try {
      ExecuteStatementResp the_response = client.executeStatement(the_request);
      if(the_response.getStatus().code == Global.FAILURE_CODE) {
        println("Failure!");
        println(the_response.getStatus().msg);
      }
      else {
        println("Success!");
        println(the_response.rowList.get(0).get(0));
      }
    } catch (TException e) {
      logger.error(e.getMessage());
    }
  }

  
  
  static Options createOptions() {
    Options options = new Options();
    options.addOption(Option.builder(HELP_ARGS)
        .argName(HELP_NAME)
        .desc("Display help information(optional)")
        .hasArg(false)
        .required(false)
        .build()
    );
    options.addOption(Option.builder(HOST_ARGS)
        .argName(HOST_NAME)
        .desc("Host (optional, default 127.0.0.1)")
        .hasArg(false)
        .required(false)
        .build()
    );
    options.addOption(Option.builder(PORT_ARGS)
        .argName(PORT_NAME)
        .desc("Port (optional, default 6667)")
        .hasArg(false)
        .required(false)
        .build()
    );
    return options;
  }

  static CommandLine parseCmd(String[] args) {
    Options options = createOptions();
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      logger.error(e.getMessage());
      println("Invalid command line argument!");
      System.exit(-1);
    }
    return cmd;
  }

  static void showHelp() {
    println("If you want to get the time: show time;");
    println("If you want to connect: connect;");
    println("If you want to disconnect: disconnect;");
    println("If you want to quit: quit;");
    println("If you want to execute sql statements, just type the one you want in a line.");
    println("For example: select name, dept_name from student where name == 'sgl' && id > 3");
  
  }

  static void echoStarting() {
    println("----------------------");
    println("Starting ThssDB Client");
    println("----------------------");
  }

  static void print(String msg) {
    SCREEN_PRINTER.print(msg);
  }

  static void println() {
    SCREEN_PRINTER.println();
  }

  static void println(String msg) {
    SCREEN_PRINTER.println(msg);
  }
}

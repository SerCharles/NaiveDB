package cn.edu.thssdb.schema;

import cn.edu.thssdb.exception.DatabaseNotExistException;
import cn.edu.thssdb.exception.FileIOException;
import cn.edu.thssdb.schema.MyErrorListener;
import cn.edu.thssdb.parser.MyVisitor;
import cn.edu.thssdb.parser.SQLLexer;
import cn.edu.thssdb.parser.SQLParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.edu.thssdb.utils.Global.DATA_DIRECTORY;

public class Manager {
    private HashMap<String, Database> databases;
    private Database currentDB;
    public ArrayList<Long> transaction_sessions;           //处于transaction状态的session列表
    public ArrayList<Long> session_queue;                  //由于锁阻塞的session队列
    public HashMap<Long, ArrayList<String>> s_lock_dict;       //记录每个session取得了哪些表的s锁
    public HashMap<Long, ArrayList<String>> x_lock_dict;       //记录每个session取得了哪些表的x锁

    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public static Manager getInstance() {
        return Manager.ManagerHolder.INSTANCE;
    }

    public Manager() {
        databases = new HashMap<>();
        s_lock_dict = new HashMap<>();
        x_lock_dict = new HashMap<>();
        currentDB = null;
        transaction_sessions = new ArrayList<>();
        session_queue = new ArrayList<>();
        recover();
    }

    public void createDatabaseIfNotExists(String databaseName) {
        try {
            lock.writeLock().lock();
            if (!databases.containsKey(databaseName))
                databases.put(databaseName, new Database(databaseName));
            if (currentDB == null) {
                currentDB = get(databaseName);
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public Database get(String databaseName) {
        try {
            lock.readLock().lock();
            if (!databases.containsKey(databaseName))
                throw new DatabaseNotExistException(databaseName);
            return databases.get(databaseName);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void deleteDatabase(String databaseName) {
        try {
            lock.writeLock().lock();
            if (!databases.containsKey(databaseName))
                throw new DatabaseNotExistException(databaseName);
            Database db = databases.get(databaseName);
            db.dropSelf();
            db = null;
            databases.remove(databaseName);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void recover()
    {
        File managerFile = new File(DATA_DIRECTORY + "manager.data");
        if (!managerFile.isFile())
            return;

        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(managerFile));
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null)
            {
                createDatabaseIfNotExists(line);
                readlog(line);
            }
            reader.close();
            bufferedReader.close();
        }
        catch (Exception e) {
            throw new FileIOException(DATA_DIRECTORY + "manager.data");
        }
    }

    public void persist()
    {
        try {
            FileOutputStream fos = new FileOutputStream(DATA_DIRECTORY + "manager.data");
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            for (String databaseName : databases.keySet())
            {
                writer.write(databaseName + "\n");
            }
            writer.close();
            fos.close();
        }
        catch (Exception e) {
            throw new FileIOException(DATA_DIRECTORY + "manager.data");
        }
    }

    public void writelog(String statement)
    {
        Database current_base = getCurrent();
        String database_name = current_base.get_name();
        String filename = DATA_DIRECTORY + database_name + ".log";
        try
        {
            FileWriter writer=new FileWriter(filename,true);
            System.out.println(statement);
            writer.write(statement + "\n");
            writer.close();
        } catch (IOException e)
        {
             e.printStackTrace();
        }
    }

    public void readlog(String database_name)
    {

        String log_name = DATA_DIRECTORY + database_name + ".log";
        File file = new File(log_name);
        if(file.exists() && file.isFile()) {
            System.out.println("log file size: " + file.length() + " Byte");
            System.out.println("Read WAL log to recover database.");
            evaluate("use " + database_name);

            try {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                ArrayList<String> lines = new ArrayList<>();
                ArrayList<Integer> transcation_list = new ArrayList<>();
                ArrayList<Integer> commit_list = new ArrayList<>();
                int index = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.equals("begin transaction")) {
                        transcation_list.add(index);
                    } else if (line.equals("commit")) {
                        commit_list.add(index);
                    }
                    lines.add(line);
                    index++;
                }
                int last_cmd = 0;
                if (transcation_list.size() == commit_list.size()) {
                    last_cmd = lines.size() - 1;
                } else {
                    last_cmd = transcation_list.get(transcation_list.size() - 1) - 1;
                }
                for (int i = 0; i <= last_cmd; i++) {
                    evaluate(lines.get(i));
                }
                System.out.println("read " + (last_cmd + 1) + " lines");
                reader.close();
                bufferedReader.close();

                //清空log并重写实际执行部分
                if (transcation_list.size() != commit_list.size()) {
                    FileWriter writer1 = new FileWriter(log_name);
                    writer1.write("");
                    writer1.close();
                    FileWriter writer2 = new FileWriter(log_name, true);
                    for (int i = 0; i <= last_cmd; i++) {
                        writer2.write(lines.get(i) + "\n");
                    }
                    writer2.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void quit()
    {
        try {
            lock.writeLock().lock();
            for (Database db : databases.values())
            {
                db.quit();
            }
            persist();
            databases.clear();
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void persistdb(String db_name)
    {
        try {
            lock.writeLock().lock();
            Database db = databases.get(db_name);
            db.quit();
            persist();
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void switchDatabase(String databaseName) {
        try {
            lock.readLock().lock();
            if (!databases.containsKey(databaseName))
                throw new DatabaseNotExistException(databaseName);
            currentDB = databases.get(databaseName);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Database getCurrent() {
        return currentDB;
    }
    
    
    private static class ManagerHolder {
        private static final Manager INSTANCE = new Manager();

        private ManagerHolder() {

        }
    }


    public String evaluate(String statement) {
        //词法分析
        SQLLexer lexer = new SQLLexer(CharStreams.fromString(statement));
        lexer.removeErrorListeners();
        lexer.addErrorListener(MyErrorListener.instance);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        //句法分析
        SQLParser parser = new SQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(MyErrorListener.instance);

        //语义分析
        try {
            MyVisitor visitor = new MyVisitor(this, -999);   //测试默认session-999
            return String.valueOf(visitor.visitParse(parser.parse()));
        } catch (Exception e) {
            return "Exception: illegal SQL statement! Error message: " + e.getMessage();
        }
    }
}

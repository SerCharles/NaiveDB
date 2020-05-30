package cn.edu.thssdb.schema;

import cn.edu.thssdb.exception.DatabaseNotExistException;
import cn.edu.thssdb.exception.FileIOException;
import cn.edu.thssdb.server.ThssDB;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.edu.thssdb.utils.Global.DATA_DIRECTORY;

public class Manager {
    private HashMap<String, Database> databases;
    private Database currentDB;
    public ArrayList<Long> transaction_sessions;
    public ArrayList<Long> session_queue;
    public HashMap<Long, ArrayList<String>> s_lock_dict;
    public HashMap<Long, ArrayList<String>> x_lock_dict;

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

    public void persistAll()
    {
        try {
            lock.writeLock().lock();
            for (Database db : databases.values())
            {
                db.quit();
            }
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
}

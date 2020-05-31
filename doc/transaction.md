# 事务模块设计文档



## 1. 文件结构

主要增加/改动以下文件：

```
main.java.cn.edu.thssdb/
	parser/
		SQL.g4
		MyVisitor.java
	server/
		ThssDB.java
	schema/
		Database.java
		table.java
		Manager.java
	service/
		IServiceHandler.java
```

其中：

- 重写SQL.g4，增加`begin transaction`和`commit`命令，同时增加了两个隐式命令`autobegin transaction`和`autocommit`，使用antlr更新visitor；
- 重构`MyVisitor.java`，增加session参数传递以区分客户端，完成事务以及恢复的相关调用接口。
- 修改`THssDB.java`使服务器支持多客户端的并发；
- 修改`table.java`，对table定义两种表级别锁和其他相关数据结构。
- 修改`Manager.java`，在其中实现申请锁与释放锁方法与管理机制，完成log生成与读取恢复功能。
- 其余文件做修改以适配相关功能与bug修复。



## 2. 实现功能

1. 服务器支持多客户端并发
2. 实现begin transaction和commit。
3. 使用二级锁协议，实现read committed隔离级别。
4. 实现单一事务的WAL机制，可以读写log并恢复数据。
5. 完善数据库存储模块与bug修改。



## 3. 功能设计和实现

### a. 多客户端支持

只需修改一行代码即可，thrift官方实现了支持多客户端的服务器。

```java
server = new TThreadPoolServer(new TThreadPoolServer.Args(transport).processor(processor));
```

##### 

### b. 事务功能实现

由于给定代码的parser不支持事务的相关语句，因此需要重构cfg文件并使用antlr重新生成相关文件。

在重构parser后，实现begin transaction和commit两个命令主要就是在MyVisitor.java中完成`visitBegin_transaction_stmt`和`visitCommit_stmt`两个方法，同时对普通语句作为一个单元素的事务看待，使用autocommit模式，在提交前后分别插入`autobegin transaction`和`autocommit`，这两个方法也需要实现（和显示方法基本一致）。

为了实现read committed级别的事务隔离，我采用了二级锁协议：

```
1.一级封锁协议
一级封锁协议是：事务T在修改数据R之前必须先对其加X锁，直到事束才释放。事务结束包括正常结束（COMMIT）和非正常结束（ROLLBACK）。
一级封锁协议可以防止丢失修改，并保证事务T是可恢复的。使用一级封锁协议可以解决丢失修改问题。
在一级封锁协议中，如果仅仅是读数据不对其进行修改，是不需要加锁的，它不能保证可重复读和不读“脏”数据。
2.二级封锁协议
二级封锁协议是：一级封锁协议加上事务T在读取数据R之前必须先对其加S锁，读完后方可释放S锁。
二级封锁协议除防止了丢失修改，还可以进一步防止读“脏”数据。但在二级封锁协议中，由于读完数据后即可释放S锁，所以它不能保证可重复读。
```

这种协议是一种表级别锁协议，在具体实现上，我在table.java中定义了以下数据：

```java
int tplock = 0;
public ArrayList<Long> s_lock_list;
public ArrayList<Long> x_lock_list;
```

其中tplock代表当前表的锁级别，可取值0，1，2分别代表无锁，s锁和x锁，`s_lock_list`和`x_lock_list`两个list储存了拥有当前表的s锁和x锁的session列表，其中由于x锁的特性，`x_lock_list`的最大长度为1。

定义了不同锁的获取、释放方法：

```java
public int get_s_lock(long session){
  int value = 0;                       //返回-1代表加锁失败  返回0代表成功但未加锁  返回1代表成功加锁
  if(tplock==2){
    if(x_lock_list.contains(session)){   //自身已经有更高级的锁了 用x锁去读，未加锁
      value = 0;
    }else{
      value = -1;                      //别的session占用x锁，未加锁
    }
  }else if(tplock==1){
    if(s_lock_list.contains(session)){    //自身已经有s锁了 用s锁去读，未加锁
      value = 0;
    }else{
      s_lock_list.add(session);         //其他session加了s锁 把自己加上
      tplock = 1;
      value = 1;
    }
  }else if(tplock==0){
    s_lock_list.add(session);              //未加锁 把自己加上
    tplock = 1;
    value = 1;
  }
  return value;
}

public int get_x_lock(long session){
  int value = 0;                    //返回-1代表加锁失败  返回0代表成功但未加锁  返回1代表成功加锁
  if(tplock==2){
    if(x_lock_list.contains(session)){     //自身已经取得x锁
      value = 0;
    }else{
      value = -1;                      //获取x锁失败
    }
  }else if(tplock==1){
    value = -1;                          //正在被其他s锁占用
  }else if(tplock==0){
    x_lock_list.add(session);
    tplock = 2;
    value = 1;
  }
  return value;
}

public void free_s_lock(long session){
  if(s_lock_list.contains(session))
  {
    s_lock_list.remove(session);
    if(s_lock_list.size()==0){       //之前就没有其他s锁
      tplock = 0;
    }else{
      tplock = 1;                //还有其他session有s锁
    }
  }
}

public void free_x_lock(long session){
  if(x_lock_list.contains(session))
  {
    tplock = 0;                  //释放x锁必然回归无锁状态
    x_lock_list.remove(session);
  }
}
```



在manager.py中，定义了一下数据结构用于锁管理：

```java
public ArrayList<Long> transaction_sessions;    //处于transaction状态的session列表
public ArrayList<Long> session_queue;           //由于锁阻塞的session队列
public HashMap<Long, ArrayList<String>> s_lock_dict;       //记录每个session取得了哪些表的s锁
public HashMap<Long, ArrayList<String>> x_lock_dict;       //记录每个session取得了哪些表的x锁
```



在MyVisitor.py中实现并修改相关接口：

```java
//开始transaction
//初始化相关状态和数据结构
public String visitBegin_transaction_stmt(SQLParser.Begin_transaction_stmtContext ctx) {
    try{
        if (!manager.transaction_sessions.contains(session)){
            manager.transaction_sessions.add(session);
            ArrayList<String> s_lock_tables = new ArrayList<>();
            ArrayList<String> x_lock_tables = new ArrayList<>();
            manager.s_lock_dict.put(session,s_lock_tables); 
            manager.x_lock_dict.put(session,x_lock_tables);
        }else{
            System.out.println("session already in a transaction.");
        }

    }catch (Exception e){
        return e.getMessage();
    }
    return "start transaction";
}
```

```java
//commit transaction
//根据记录释放相关表的x锁，清空相关记录并查看log文件大小，超过一定大小后持久化数据并清空log
public String visitCommit_stmt(SQLParser.Commit_stmtContext ctx) {
    try{
        if (manager.transaction_sessions.contains(session)){
            Database the_database = GetCurrentDB();
            String db_name = the_database.get_name();
            manager.transaction_sessions.remove(session);
            ArrayList<String> table_list = manager.x_lock_dict.get(session);
            for (String table_name : table_list) {
                Table the_table = the_database.get(table_name);
                the_table.free_x_lock(session);
                the_table.unpin();
            }
            table_list.clear();
            manager.x_lock_dict.put(session,table_list);

            //查看log文件大小，超过一定大小后持久化数据并清空log
            String log_name = DATA_DIRECTORY + db_name + ".log";
            File file = new File(log_name);
            if(file.exists() && file.isFile() && file.length()>50000)
            {
                System.out.println("Clear database log");
                try
                {
                    FileWriter writer=new FileWriter(log_name);
                    writer.write( "");
                    writer.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                manager.persistdb(db_name);
            }
        }else{
            System.out.println("session not in a transaction.");
        }
        //System.out.println("sessions: "+manager.transaction_sessions);
    }catch (Exception e){
        return e.getMessage();
    }
    return "commit transaction";
}
```

`autobegin transaction`和`autocommit`与以上实现类似。

同时修改insert，delete，update，select方法的接口，具体代码不在此处展示，主要思路是对于一个新的transaction，尝试获取锁，如果获取锁失败则加入等待队列，在等待队列开头的事务会定期尝试获取锁，其余事务处于阻塞休眠状态。如果获取相应的锁成功则出队执行操作，如果是select操作，则执行完操作后立刻释放s锁，其余操作等待commit后再释放x锁。



### c. WAL机制实现

wal机制的主要思路：将对表的修改语句记录写入log文件，这样数据就不需要立即持续化，而是定期由某种机制持久化，同时清空log文件；在重新启动数据库时，数据库会根据log的记录进行数据库的恢复，当log中存在某个事务未完成（begin transaction语句和commit语句数目不等）时，执行到开始事务之前的一句，并把log中未完成事务的记录删除，这样就保证了事务的原子性。

相关接口主要在manager.java中实现：

```java
//写数据，只在更改数据库时记录，读取数据库时不记录
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
```



```java
//读取log并恢复数据库
//如果遇到执行不完整的transaction，则执行到事务开启之前的语句，不完整的事务语句清除。
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
```

通过这样的机制，即可实现数据库的恢复与事务的原子性保证。



将B+树从Table中移入Cache类中，其他与框架没有变化。

#### 成员函数

- 构造器：参数与框架相同；在构造其中，保存每一列的信息以及主键位置，调用recover方法从文件中恢复持久化的数据。
- 查询记录：`public Row get(Entry entry)` 传入查询的记录的主键作为参数，通过调用Cache类提供的接口进行查询。
- 删除记录：`public void delete(Entry primaryEntry)` 传入待删除的记录的主键作为参数，通过调用Cache类提供的接口进行删除。
- 增加记录：`public void insert(ArrayList<Column> columns, ArrayList<Entry> entries)` 传入待插入的行的列信息和记录信息作为参数，检查每一列和表的schema相同后调用Cache类提供的接口进行增加。
- 修改记录：`public void update(Entry primaryEntry, ArrayList<Column> columns, ArrayList<Entry> entries)` 传入主键、待修改的列及对应的数据作为参数，检查每一个待修改的列位于表的schema中后调用Cache类提供的接口进行修改。
- 恢复数据：`private void recover()` 在默认的文件目录下找到本表格对应的文件并调用Cache类的方法进行恢复。
- 持久化数据：`public void persist()` 直接调用Cache类的方法，将所有页面保存到文件中。

### b. Cache包

Cache包包含Cache类、Page类以及Cache类的内部类EmptyRow类（继承自Row），主要负责内存的管理，包括页面置换等工作。

#### 1) 设计思路

在页式存储的前提下，如何在内存中保存记录，考虑并测试了两种实现方案：

##### 方案一：

将记录(Row)保存在页面(Page类)上，修改框架提供的B+树index的实例化方式，不再从主键Entry映射到Row，而是从Entry映射到保存这条记录的页面编号及页内位置上，每次增删改查操作时，先在B+树种查询位置，再到对应的Page上完成相应操作。

##### 方案二：

保留框架提供的B+树index的实例化方式，在内存中仍然把Row作为B+树的Value保存；在页面上只存储记录的主键和记录的长度信息，每次查询直接在index中完成，需要页面置换、保存、恢复时再通过Page中记录的主键到index中获取对应的Row并进行保存/恢复。

##### 比较和选择

两种方案各有优缺点。对于方案一，优点是除了删除操作，不需要对B+树进行大的更新，同时页面置换也与B+树无关，因此在测试中对于遍历数据库2000条记录的操作，这种方案平均用时比方案二少200ms；缺点在于一旦涉及到删除，需要对同属一个页面的其他记录也更新位置信息，一旦实际使用的页面较大（测试时为了测试页面置换的正确性和效率采用了256Byte的很小的页面大小），同时更新的记录会很多，而且对于其他操作，获取记录都需要两部，即先获取位置再访问页面，对单条记录的访问效率较低。对于方案二，优点是对于较大的页面比较友好，查询、修改、增加记录都没有额外操作；但是对于页面置换来说，需要修改整个页面对应的所有记录。

综上，考虑到实际实现中页面大小远大于测试使用的大小，同时从访问记录的用时上考虑，最终选择了方案二，即保留框架中index的实例化方式，在B+树中存储记录的方案。

#### 2) 实现方式

##### 多客户端支持

只需修改一句话即可，thrift官方实现了支持多客户端的服务器。

```java
server = new TThreadPoolServer(new TThreadPoolServer.Args(transport).processor(processor));
```

##### Page类

```java
public class Page {
    public static final int maxSize = 256;
    private int id;               // unique id
    private int size;             // size of the page
    private ArrayList<Entry> entries;  // primary keys
    private String pageFileName;  // filename on the disk
    private long timeStamp;       // timestamp of last visit
    private Boolean edited;       // this page has been edited, need to be write back to file
}
```

##### 页式存储管理方式

在Cache类中保存每个页面编号对应的Page，并记录当前页面数量，当页面数量超过上限时，就开始执行置换：

- expelPage

  遍历当前在内存中的所有页面，根据LRU算法选择时间戳最早的一个进行驱逐。将该页面所对应的记录在index中置为EmptyRow，同时将原来的Row存入文件中。

- readPage

  从文件中读入一个页面对应的行，用主键在index中更新记录，并在Cache类中恢复该页的状态。

- exchangePage

  遇到访问的记录在index中是EmptyRow的情况时，先选择一个页面驱逐，再读入对应的页面对该记录（及所有该页面上的记录）进行恢复。

## 4. 测试情况

由于事务模块主要需要人为观察数据库运行结果，并没有编写自动化单元测试代码，下面是我手动进行的一些操作：

#### 单事务测试

测试了begin transaction和commit命令，在其中执行多个语句，发现结果运行正常，处于事务状态时，客户端会显示处于(T模式)。

#### 多事务并发测试

开启多个客户端事务，具有以下特点：

1. 当一个事务只执行读取操作时，并不影响其余事务对table的读取。
2. 当一个事务执行了对table的修改但未commit，此时其余事务的读写操作都无法获取锁，被阻塞等待。当commit相关操作后，会按照阻塞事务的操作加入时间选择下一个执行的事务。
3. 结果表示，相关操作解决了脏读问题，但不能保证两次读取结果一致，实现了read committed级别的隔离。

#### 单事务WAL恢复

关闭数据库，发现此时储存的二级制文件并没有改变，重启数据库，数据库会根据log文件执行相关操作恢复数据库，查看相关信息发现数据库恢复无误。

当完成一个transaction后且log文件大小大于5MB时，会自动清空log文件且对数据进行持久化。

#### 事务原子性

在事务执行一半未commit时，关闭数据库程序以模仿掉电故障，重启数据库查看数据，可发现数据库已回退到未执行事务之前的状态。

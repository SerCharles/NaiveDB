# 存储模块设计文档



## 1. 文件结构

主要增加/改动以下文件：

```
main.java.cn.edu.thssdb/
	cache/
		Cache.java
		Page.java
	schema/
		Table.java
		
test.java.cn.edu.thssdb/
	storage/
		TableTest.java
```

其中：

- 新增了cache包，实现了页式存储管理的缓存系统，以LRU算法作为页面置换算法；
- 实现了原schema包中Table类的所有方法；
- 在测试目录下增加了对存储系统的单元测试TableTest类。



## 2. 实现功能

1. 记录的持久化（保存到文件和从文件中恢复记录）
2. 对记录的增删改查
3. 五种数据类型 Int, Long, Float, Double, String
4. 页式存储格式



## 3. 类的设计和实现

### a. Table类

因为页式存储较为复杂，涉及到页面的选择、置换等与数据库本身逻辑无关的工作，因此参考了hsqldb的思路，单独实现了管理内存的Cache类，从而将内存的管理从Table类中剥离了；在Table类中，只处理与数据库本身逻辑相关的工作，一切涉及内存的内容通过调用Cache类提供的接口完成。

#### 成员变量

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

##### Cache类

```java
public class Cache {
    private static final int maxPageNum = 100;
    private HashMap<Integer, Page> pages;
    private int pageNum;
    private BPlusTree<Entry, Row> index;
    private String cacheName;
    
    // 公有接口
    public Cache(String databaseName, String tableName);
    public Iterator<Pair<Entry, Row>> getIndexIter();
    public boolean insertPage(ArrayList<Row> rows, int primaryKey);
    public void insertRow(ArrayList<Entry> entries, int primaryKey);
    public void deleteRow(Entry entry, int primaryKey);
    public void updateRow(Entry primaryEntry, int primaryKey,
                          int[] targetKeys, ArrayList<Entry> targetEntries);
    public Row getRow(Entry entry, int primaryKey);
    public void persist();
    
    // 页面选择、置换相关私有接口
    private void exchangePage(int pageId, int primaryKey);
    private boolean addPage();
    private void expelPage();
    private void serialize(ArrayList<Row> rows, String filename);
    private ArrayList<Row> deserialize(File file);
    
    // 内部类 EmptyRow
    public class EmptyRow extends Row {
        public EmptyRow(int position)
        {
            super();
            this.position = position;
        }
    }
}
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

在test/实现了对存储模块的单元测试，可通过 `mvn test` 执行。

#### 增加（访问）测试

插入了2000条记录，随机抽取若干记录访问并测试了记录内容的正确性。对不存在的主键进行了访问的测试，会抛出KeyNotExisted异常并提示不存在的主键名。

#### 遍历测试

遍历所有2000条记录，测试了遍历的顺序是有序的，同时记录的内容是正确的。

#### 更新测试

随机抽取了若干记录进行更新，测试了包括更新主键、不更新主键的情况；以及更新后的主键已经存在等情况，对于后者，会抛出DuplicatedKey异常并提示重复的主键名。

#### 删除测试

随机抽取了几个主键删除，测试包括了待删除的记录的主键存在和不存在的情况；前者能够正确删除，后者会抛出KeyNotExisted异常并提示不存在的主键名。

#### 持久化/恢复测试

经过上述增删改查后，对表进行持久化，然后从文件中恢复。测试访问了经过上述测试后某些被修改的记录、被删除的记录、以及额外增加的记录，内容均正确。
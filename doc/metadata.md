# 元数据管理模块设计文档



## 1. 文件结构

主要增加/改动以下文件：

```
main.java.cn.edu.thssdb/
	schema/
		Table.java
		Manager.java
		Database.java
		
test.java.cn.edu.thssdb/
	metadata/
		DBManagerTest.java
```

其中：

- Database.java 中为管理表的类
- Manager.java 中为全局管理所有数据库的类
- DBManagerTest.java 为测试元数据管理模块的代码



## 2. 实现功能

1. 表的创建、删除、修改
2. 数据库的创建、删除、切换
3. 表元数据的持久化，数据库元数据持久化
4. 启动数据库时从文件恢复数据库以及对应的表



## 3. 类的设计和实现

### a. Database类

#### 主要成员变量

- `tables` 用哈希表以表的名字为键存储数据库对应的所有表
- `name` 数据库本身的名字

#### 主要成员函数

##### 公有方法

- `public void create(String name, Column[] columns)` 传入表的名字和表头信息，创建一张新表；如果名字已存在，会抛出`DuplicateTableException` 异常。
- `public Table get(String name)` 根据表的名字获取一张表；如果名字不存在，会抛出`TableNotExistException` 异常。
- `public void drop(String name)` 根据表的名字删除一张表，包括在数据库中的记录、内存中的信息以及外存中的文件；如果名字不存在，会抛出`TableNotExistException` 异常。
- `public void dropSelf()` 删除自身这个数据库，包括其中的每一张表和数据库的元数据文件。
- `public void quit()` 退出系统时调用，对数据库的元数据以及每一张表进行持久化。

##### 私有方法

- `private void persist()` 持久化方法，将数据库的元数据保存在以meta_开头的外存文件中。
- `private void recover()` 恢复方法，从外存中读取对应该数据库的元数据文件，并根据内容创建相应的表。

### b. Manager类

#### 主要成员变量

- `databases` 使用哈希表以数据库的名字为键存储所有的数据库
- `currentDB` 当前数据库

#### 主要成员函数

##### 公有方法

- `public static Manager getInstance()` 静态方法获得全局唯一的Manager实例对象。
- `public void createDatabaseIfNotExists(String databaseName)` 根据数据库名创建数据库，供外部调用以及自身的`recover`方法调用。
- `public Database get(String databaseName)` 根据数据库名获取数据库对象；如果名字不存在，会抛出`DatabaseNotExistException` 异常。
- `public void deleteDatabase(String databaseName)` 根据数据库名删除数据库对象；如果名字不存在，会抛出`DatabaseNotExistException` 异常。
- `public void switchDatabase(String databaseName)` 根据数据库名切换当前数据库；如果名字不存在，会抛出`DatabaseNotExistException` 异常。
- `public void quit()` 退出系统，会自动调用`persist()` 保存manager的元数据到外存文件，并调用所有database的`quit()`方法进行退出和保存。

##### 私有方法

- `private void persist()` 持久化，将manager的元数据，包括数据库名等数据保存到外存文件。
- `private void recover()` 从文件中恢复manager的元数据，并调用`createDatabaseIfNotExists` 创建所有的表。



## 4. 单元测试

在`test.java.cn.edu.thssdb/metadata/`目录下实现了对元数据管理模块的单元测试，可通过`mvn test`执行。

### 初始化

创建了一个管理者Manager类的实例对象，在manager中创建了三个数据库University, HighSchool, MiddleSchool，每个数据库中创建了Student和Teacher两张表。

### 添加记录

向每一张表中添加2000条记录，并随机访问若干条记录检查正确性。

### 删除

- 删除数据库

  删除MiddleSchool数据库，检查是否还能访问已删除内容

- 删除表

  删除University数据库中的Teacher表，检查是否还能访问已删除内容

- 删除记录

  删除University数据库中的Student表的1000条记录，检查是否还能访问已删除内容

### 持久化和恢复

先调用manager的 `quit()` 方法退出系统，将所有信息保存在外存，再进行恢复。检查在删除测试中删除的内容是否能访问，以及未删除的内容是否正确。
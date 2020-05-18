# 存储模块设计文档

## 1. 文件结构

主要增加/改动以下文件：

```
main.java.cn.edu.thssdb/
	query/
	parser/
	schema/
		Table.java
		Database.java
	type/
		ComparerType.java
		ConditionType.java
		ConstraintType.java
		LiteraiType.java
		LogicType.java
		ResultType.java
test.java.cn.edu.thssdb/
	query/
		QueryTest.java
		SQLTest.java
```

其中：

- 继承了原parser包的SQLVisitor实现了MyVisitor，以实现语义分析。而且新建文件SQLHandler来调用词法，句法，语义分析，还有编译异常处理。
- 实现了原query包中QueryTable，QueryResult等类
- 在测试目录下增加了对查询的单元测试QueryTest，SQLTest类。



## 2. 实现功能

1. SQL的词法，句法，语义分析
2. Database,Table类中数据库的增加，删除，切换，显示；表的增加，删除，显示；还有数据的增删改查接口
3. 实现了QueryTable，QueryResult类，实现了任意张表的join和distinct关键字
4. 实现了逻辑模块，能够完成（on和where中）多个比较条件的and，or连接，而且实现了sql的null逻辑

## 3. 类的设计和实现

### a. 逻辑模块

逻辑模块在实现删除，更新，查询的where语句，以及多表连接的on语句中都很重要。

一个逻辑表达式（multiple_condition），实际上是0个到多个条件表达式（condition）连接成的，SQL的语法树是递归定义这种连接的

```g4
multiple_condition :
    condition
    | multiple_condition AND multiple_condition
    | multiple_condition OR multiple_condition ;
```

而一个condition对象，则是由两个运算表达式（expression）连接成的，SQL语法树实现如下：

```
condition :
    expression comparator expression;
```

但是我并没有实现运算表达式，运算表达式我直接使用一个元素（comparer）来替代了。

```
expression :
    comparer
```

因此，我定义了三个对应的类：

**Logic类**：一个完整或者部分完整的逻辑，它可能是只有一个condition的，也可能由左右两个逻辑连接而成。

```java
    public Logic(Logic left, Logic right, LogicType type) {
        this.mTerminal = false;
        this.mLeft = left;
        this.mRight = right;
        this.mType = type;
    }

    public Logic(Condition condition) {
        this.mTerminal = true;
        this.mCondition = condition;
    }

```

**Condition类**：一个比较表达式，由左右两个comparer连接而成。

```java
	public Condition(Comparer left, Comparer right, ConditionType type) {
        this.mLeft = left;
        this.mRight = right;
        this.mType = type;
    }
```

**Comparer类**：一个值包装器，只有值和类型。

```java
    public Comparer(ComparerType type, String value) {
        this.mType = type;
        switch (type) {
            case NUMBER:
                this.mValue = Double.parseDouble(value);
                break;
            case STRING:
            case COLUMN:
                this.mValue = value;
                break;
            default:
                this.mValue = null;
        }
    }
```

通过Logic类的GetResult方法，我实现了一个逻辑表达式对一个广义数据行（JointRow）（一个行或者多个行连接而成的行）的逻辑判定。在Logic类里，我进行递归判定，用左右子逻辑/自己唯一的条件表达式的值求得当前逻辑的值；在Condition类里，我先比较左右两个比较器的类型，如果有COLUMN类型，就去数据行里读取对应的值，并且更新类型。之后按照比较器类型进行比较和异常处理。值得一提的是，我在这里实现了sql的null逻辑：null和谁比较都是unknown，unknown的逻辑运算我也实现了。

### c. Parser和语义分析

词法，句法分析部分，我使用框架提供的SQL.g4,SQLLexer,SQLParser实现。语义分析部分，我继承了框架提供的SQLVisitor实现，实现于MyVisitor，通过递归解析语法树，调用Table，Database等类的代码实现。我实现了SQLHandler来调用词法，句法，语义分析，编译器异常处理部分，代码如下：

```java
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
			MyVisitor visitor = new MyVisitor(manager);
			return String.valueOf(visitor.visitParse(parser.parse()));
		} catch (Exception e) {
			return "Exception: illegal SQL statement! Error message: " + e.getMessage();
		}
	}
```

语义分析遇到的主要困难是insert，delete，update，select的类型问题了。在insert中，所有属性值被解析为string类型，我直接使用五大类型对应parser来将其转换成想要的。在update，delete，select中，所有属性值都被解析为comparer类型，因此我只进行了string，null，column的判断，对于数字则是直接强制类型转换成double类型，再强制类型转换成所需的。因此，因为语法树的规定，这些地方我没有做仔细的数字类型判断。

## 4. 测试情况

在test/实现了对查询模块的单元测试，可通过 `mvn test` 执行。

#### 逻辑测试

位置在test中的query/QueryTest类/Logictest函数

我手工构造了13条逻辑，包括空，单一逻辑，复合逻辑等多种情况，用他们判断一些列是否满足这些逻辑。

#### 增删改查测试

位置在test中的query/QueryTest类

我构造了三个表，以及对应的各种插入情况，更新情况，where逻辑，on逻辑，通过直接调用database中的增删改查函数来判断这些的实现是否有误。

#### SQL测试

位置在test中的query/SQLTest类

我直接构造了若干条sql语句，对应作业需求的数据库，表，数据的各种操作，用于检测这些操作是否正确。
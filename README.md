# NaiveDB

![Java CI with Maven](https://github.com/JianyuTANG/NaiveDB/workflows/Java%20CI%20with%20Maven/badge.svg)

#### 文件结构

src：源代码，其中main是项目代码，test是测试文件

bin：可执行文件，其中server为服务器，client为客户端

doc：设计文档，用户文档，展示ppt所在地

out：如果用idea打开并且生成artifact，能在这里找到可执行文件

**本项目使用Java8，在idea编程环境下开发，请先安装对应的环境再运行**

#### 可执行文件运行方法

首先清空bin/data下全部文件，然后命令行进入bin目录下

运行server：

```shell
java -jar server.jar
```

运行client：

```shell
java -jar client.jar
```

#### 项目运行方法

先进入项目根目录下，命令行运行

```shell
mvn clean package
```

然后用idea打开项目，设置jdk为java8，然后build即可。其中ThssDB类是服务器，Client类是客户端。我们的客户端支持多客户端运行，可以在idea下设置。

#### 单元测试运行方法

进入项目根目录下，命令行运行

```shell
mvn test
```


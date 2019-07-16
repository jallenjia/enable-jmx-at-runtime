## enable-jmx-at-runtime (enable-jmx-without-restart)
Ability of our tool: Enable JMX port when a Java app is running, without restart the app.

### When will you need it?
JMX is a Java technology that supplies tools for managing and monitoring applications, system objects, etc.
For instance JVisualVM, a builtin tool in JDK. It could connect to app's **JMX port**, monitoring the usage rate of CPU or MEM, and even CPU Profiling for performence optimizing.

In general, in order to start the JMX port, you need to add the JMX startup parameter when the Java application starts.Examples are as follows:
```shell
java -Djava.rmi.server.hostname=192.168.66.38 \
    -Dcom.sun.management.jmxremote.port=17777 \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    com.example.Main
```

But the problem is: Java apps in test environments often don't set JMX parameters.
When you encounter a performance problem, and need to connect to JMX to do CPU Profiling, you must add the JMX parameter and restart the apps. By the time everything is ready, the problem may be never appear again; Or you have to take extra hours to construct the problem.

How to solve problems above? 
Enable the JMX at runtime, with our tool, and do analyze right now.


### Usage
[download tool in release page](../../releases)，deploy it on the computer running a Java app。

Suppose you have a running Java app, process ID 9999, started by the user dreamers.
You can enable JMX port as below:
```shell
# Switch user to dreamers
su - dreamers

# For deatailed usage.
sh enable_jmx.sh

# Usage 1: enable JMX on port 17777, **without authentication, without ssl**
# COMMENT: you could omit PORT=xxx, then use the default port 17777
# After this command, JVisualVM could connector to JMX port
sh enable_jmx.sh PID=9999 PORT=17777

# Usage 2: set JMX port by conf. In the conf file, you may set JMX PORT, authentication, ssl, etc.
# Conf template could copy from: ${JAVA_HOME}/jre/lib/management/management.properties
# Full doc of the config: https://docs.oracle.com/javase/6/docs/technotes/guides/management/agent.html#gdeum
sh enable_jmx.sh PID=9999 CONF=./management.properties
```

### Principle
Oracle jdk builtin lib rt.jar，has a class to start JMX: sun.management.Agent
Our tool let target Java app load the agent, to enable JMX.

Full doc of the config: https://docs.oracle.com/javase/6/docs/technotes/guides/management/agent.html#gdeum


***

## Java程序运行时开启JMX（零停机开启JMX）
功能：在Java应用运行时，启动JMX端口。无需重新启动应用

### 应用场景
JMX是Java平台的技术，用于管理、监控Java应用。
比如JDK自带JVisualVM工具，可连接Java应用的**JMX端口**，监测内存CPU使用率，统计耗CPU的方法（作为性能优化依据）。


通常，为了启动JMX端口，需要在Java应用启动时，就加上JMX启动参数。举例如下：
```shell
java -Djava.rmi.server.hostname=192.168.66.38 \
    -Dcom.sun.management.jmxremote.port=17777 \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    com.example.Main
```

但问题是：测试环境的Java应用，通常没有加上JMX启动参数。
当遇到性能问题，需要连接JMX，统计过分消耗CPU的方法时，你必须加上JMX参数并重启应用。等一切就绪时，问题可能已经不复现了；或者要额外再花半小时，来复现问题。

如何解决上述问题？当然是不重启Java应用，直接通过工具开启JMX端口，然后开始定位工作。
这个工具，就是用来解决这个问题的。

### 用法
[release页下载工具](../../releases)，放到运行Java应用的主机上（任意目录），解压。
假设有个运行中的Java应用，进程ID 9999，该进程由用户dreamers启动。你可以用如下方法，启动进程的JMX端口：
```shell
# 切换用户到dreamers
su - dreamers

# 【查看命令帮助】
sh enable_jmx.sh

# 【用法1】开启应用JMX端口，JMX端口17777，**无鉴权，不是ssl连接**
# 备注：PORT=xxx 可以省略，默认端口17777
# 执行完后，可以用JVisualVM等工具，连接JMX端口
sh enable_jmx.sh PID=9999 PORT=17777

# 【用法2】通过配置文件启动JMX端口。配置文件中，设定JMX端口、鉴权、ssl等信息。
# 配置文件模板，可以从以下目录拷贝：${JAVA_HOME}/jre/lib/management/management.properties
# 配置项解释，见：https://docs.oracle.com/javase/6/docs/technotes/guides/management/agent.html#gdeum
sh enable_jmx.sh PID=9999 CONF=./management.properties
```

### 原理
oracle jdk自带rt.jar，其中有类：sun.management.Agent，用于启动JMX端口。
本工具，让目标应用，load上述Agent，实现启动JMX的任务。

sun.management.Agent接受的配置项，见https://docs.oracle.com/javase/6/docs/technotes/guides/management/agent.html#gdeum

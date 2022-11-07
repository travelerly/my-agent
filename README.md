# JavaAgent

## JavaAgent 介绍

JavaAgent 是一种能欧在不影响正常编译的情况下，修改字节码的技术。JavaAgent 是 JDK 1.5 以后引入的，也可以叫做 Java 代理。JavaAgent 是运行在 main 方法之前的拦截器。



## JavaAgent 启动方式

JavaAgent 的启动方式分为静态启动和动态启动两种。



### 静态启动

使用 `-javaagent` + 启动参数的方式启动，入口方法是 `premain()` 方法，JavaAgent 是运行在 `main()` 方法之前的拦截器，也就是先执行 `premain()` 方法，然后再执行 `main()` 方法。即在类加载时，对目标类的字节码可以进行任意的修改，只要最后的结果符合字节码规范即可。Skywalking 就是使用的静态启动方式启动 Agent，且仅支持此方式。

javaagent 是 java 命令的一个参数，可以用于指定一个 jar 包，并且对该 java 包有 2 个要求：

1. 这个 jar 包的 MANIFEST.MF 文件必须指定 Premain-Class 项；
2. Premain-Class 指定的那个类必须实现 `premain()` 方法。



### 动态启动

使用 Attach API 的方式启动，入口方法是 agentmain() 方法。即类已经加载完成被使用，这时只能对目标类的字节码进行有限的修改（不能增减父类、不能增加接口、不能调整 Field……）。

典型的应用就是阿里的系统诊断工具 Arthas。





## JavaAgent 的实际应用

1. 可以在加载 java 文件之前做拦截，把字节码做修改；
2. 获取所有已经被加载过的类；
3. 获取某个对象的大小；
4. 将某个 jar 加入到 bootstrapclasspath 里作为高优先级被 bootstrapClassLoader 加载；
5. 将某个 jar 加入到 classpath 中供 AppClassLoader 加载；
6. 设置某些 native 方法的前缀，主要在查找 native 方法的时候做规则匹配。



## JavaAgent 案例

### 案例一

#### colin-agent-jar 项目

普通 maven 工程，该项目主要编写 premain 方法，并最终将该项目打成 jar 包，以便目标项目引入使用。

```java
package com.colin.agent;

public class PremainDemo {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("hello agent，双参数 premain");
        System.out.println("agentArgs===>" + agentArgs);
    }
}
```



#### MANIFREST.MF 文件

在 resources 目录下新建目录 META-INF，在该目录下创建 MANIFREST.MF 文件，文件内容如下：

```
Manifest-Version: 1.0
Premain-Class: com.colin.agent.PremainDemo
Agent-Class: com.colin.agent.PremainDemo
Can-Redefine-Classes: true
Can-Retransform-Classes: true
Build-Jdk-Spec: 1.8
Created-By: Maven Jar Plugin 3.2.0
```

> 文件内容所属含义：
>
> - Premain-Class：包含 `premain()` 方法的类的全路径名称；
> - Agent-Class：包含 `agentmain()` 方法的类的全路径名称；
> - Boot-Class-Path ：设置引导类加载器搜索的路径列表。查找类的特定于平台的机制失败后，引导类加载器会搜索这些路径。按列出的顺序搜索路径。列表中的路径由一个或多个空格分开。路径使用分层 URI 的路径组件语法。如果该路径以斜杠字符（“/”）开头，则为绝对路径，否则为相对路径。相对路径根据代理 JAR 文件的绝对路径解析。忽略格式不正确的路径和不存在的路径。如果代理是在 VM 启动之后某一时刻启动的，则忽略不表示 JAR 文件的路径（可选）；
> - Can-Redefine-Classes：表示是否能重定义此代理类所需的类，默认为 fasle（可选）；
> - Can-Retransform-Classes：表示是否能重转换此代理类所需的类，默认为 false（可选）；
> - Can-Set-Native-Method-Prefifix：表示是否能设置此代理类所需的本地方法前缀，默认为 false（可选）。



不推荐使用手工编写，比较麻烦。可以使用插件生成，使用 maven-shade-plugin 插件进行打包

```xml
<build>
    <finalName>colin-agent-jar</finalName>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.2.4</version>
            <configuration>
                <createDependencyReducedPom>false</createDependencyReducedPom>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <transformers>
                            <transformer
                                    implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <manifestEntries>
                                    <!--premain 方法所在的类的全限定名称-->
                                    <Premain-Class>com.colin.agent.PremainDemo</Premain-Class>
                                </manifestEntries>
                            </transformer>
                        </transformers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

> 打包命令：mvn clean package
>
> 使用 maven 插件将 colin-agent-jar 项目打成 jar 包，以便目标项目引入使用。



#### colin-agent-test 项目

普通 maven 工程，该项目主要编写 main() 方法。

```java
package com.colin.agent;

public class AgentTest {
    public static void main(String[] args) {
        System.out.println("这是 javaagent 第一个 main 方法测试");
    }
}
```



#### IDEA 配置

必须先运行一次 `AgentTest#main()` 方法，然后才能在 IDEA 中出现编辑启动类选项。

在 edit confifigurations 的 VM options 中输入测试参数，主要是 javaagent 命令 + agent 的 jar 包全路径，jar 包后面可以填写参数，以 `=` 链接，即 premain() 方法的参数，也可以不填写。

```sh
-javaagent:/Users/colin/workspace-java/my-project/my-agent/colin-agent-jar/target/colin-agent-jar.jar=colin
```



#### 案例一测试

运行  `AgentTest#main()` 方法，测试结果如下：

```txt
hello agent，双参数 premain
agentArgs===>colin
这是 javaagent 第一个 main 方法测试
```

即在 `AgentTest#main()` 运行之前，执行了 `PremainDemo#premain()` 方法，实现了 JavaAgent 的效果。



### 案例二

整理中
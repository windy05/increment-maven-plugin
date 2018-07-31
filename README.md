# maven-release-increment
svn作为代码管理工具的war包项目增量更新maven插件。基于svnkit获取对应startRevision的版本到最新版本的文件差异。
然后将项目生成目录下的文件进行过滤，将差异文件进行打包到指定目录下。

Getting Started
---------------
clone 源码 执行mvn install。

本地项目添加插件，pom文件配置如下
```
<plugin>
    <groupId>org.bsoft.tj.plugins</groupId>
    <artifactId>increment-maven-plugin</artifactId>
         <configuration>
              <url>https://xxxx/xxxx/xxxx/xxx</url>
                <targetPaths>
                    <targePath>/xxxx</targePath>
                </targetPaths>
                <userName>userName</userName>
                <password>password</password>
                <startRevision>149962</startRevision>
         </configuration>
         <executions>
             <execution>
                 <goals>
                     <goal>release</goal >
                 </goals>
             </execution>
         </executions>
</plugin>
```
+ url：svn版本库地址
+ userName ：svn用户名
+ password：svn密码
+ startRevision：开始svn提交版本号

执行 mvn clean package 即可。

默认目录${project.build.directory}/increment-release
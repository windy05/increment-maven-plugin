# maven-release-increment
根据maven-scm-plugin的diff命令形成比对文件（outputFile），maven-war-plugin生成打包文件夹（webappDirectory），筛选出需要的增量更新文件。


Getting Started
---------------
clone 源码 执行mvn install。

本地项目添加插件，pom文件配置如下
```
<scm>
    <connection>scm:svn:https://xxx.xxx.xxx.xxx/svn/</connection>
</scm>
```
```
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-scm-plugin</artifactId>
	<version>1.10.0</version>
	<configuration>
		<connectionType>connection</connectionType>
		<startScmVersionType>revision</startScmVersionType>
		<startScmVersion>140792</startScmVersion>
	</configuration>
	<executions>
	    <execution>
			<phase>package</phase>
	    		<goals>
					<goal>diff</goal>
                        </goals>
	    </execution>
	</executions>
</plugin>
<plugin>
	<groupId>org.bsoft.tj.plugins</groupId>
	    <artifactId>increment-maven-plugin</artifactId>
	    <version>2.0-SNAPSHOT</version>
	   	<executions>
			<execution>
			<phase>package</phase>
			<goals>
				<goal>increment</goal>
	         </goals>
            </execution>
	    </executions>
</plugin>
```
+ connection：版本库地址(支持svn，git等scm)
+ startScmVersion：比对开始提交版本号

具体配置可参考maven-scm-plugin与maven-war-plugin官方配置

执行 mvn clean package 即可。

默认目录${project.build.directory}/increment-release

尚未解决问题
---------------
+ maven 依赖修改尚无法比对，增量打包
+ maven-scm-plugin 中 outputFile 参数修改需要在 increment-maven-plugin同时修改 outputFile参数
+ maven-war-plugin 中 webappDirectory与warSourceDirectory 同上
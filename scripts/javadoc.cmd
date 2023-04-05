@echo off
setlocal

REM (JDBC 3 version removed as of 2015-09-30)
REM invoke the 1.6 jvm for the JDBC 4 version

SET ANT_HOME=C:\JavaDev\apache-ant-1.10.12
set ANT_OPTS=-Xmx512m

SET JAVA_HOME=C:\JavaDev\jdk-8.0.222.10-hotspot
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin

call java -version
call ant -Djdbc.level=4 -Djvm.ver=1.8 javadoc

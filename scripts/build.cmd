@echo off
setlocal

REM (JDBC 3 version removed as of 2015-09-30)
REM invoke the 1.6+ jvm for the JDBC 4 version

SET ANT_HOME=C:\JavaDev\apache-ant-1.10.12
SET JAVA_HOME=C:\JavaDev\jdk-8.0.222.10-hotspot
SET PATH=%JAVA_HOME%\BIN;%ANT_HOME%\BIN

call java -version
call ant -Djdbc.level=4 -Djvm.ver=1.8 all

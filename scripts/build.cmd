@echo off
setlocal

REM (JDBC 3 version removed as of 2015-09-30)
REM invoke the 1.6 jvm for the JDBC 4 version

set ANT_HOME=c:\apache-ant-1.8.1
set JAVA_HOME=C:\jdk\1.6.0_21
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin

call java -version
call ant -Djdbc.level=4 -Djvm.ver=1.6 all

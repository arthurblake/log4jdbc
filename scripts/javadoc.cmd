@echo off
setlocal

REM invoke the 1.4 and the 1.6 jvm each in turn 
REM for JDBC 3 and JDBC 4 versions respectively

set ANT_HOME=c:\apache-ant-1.8.1
set ANT_OPTS=-Xmx512m

set JAVA_HOME=C:\jdk\1.4.2_19
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin

call java -version
call ant -Djdbc.level=3 -Djvm.ver=1.4 ydoc.3

set JAVA_HOME=c:\jdk\1.6.0_18
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin

call java -version
call ant -Djdbc.level=4 -Djvm.ver=1.6 ydoc.4

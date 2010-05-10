@echo off
setlocal

REM invoke the 1.4 and the 1.6 jvm each in turn 
REM for JDBC 3 and JDBC 4 versions respectively

REM BTW, It's not actually necessary to use JDK 1.4
REM if you don't happen to have it laying around,
REM as the ant script properly sets the source
REM and target java versions anyway.

set ANT_HOME=c:\apache-ant-1.8.1

set JAVA_HOME=C:\jdk\1.4.2_19
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin

call java -version
call ant -Djdbc.level=3 -Djvm.ver=1.4 all

set JAVA_HOME=C:\jdk\1.6.0_18
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin

call java -version
call ant -Djdbc.level=4 -Djvm.ver=1.6 all

@echo off
setlocal

rem run ydoc for jdbc 3 with jdk 1.4 and
rem run ydoc for jdbc 4 with jdk 1.6

set ANT_HOME=c:\apache-ant-1.6.5

set JAVA_HOME=C:\j2sdk1.4.2_13
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin
call java -version

call ant -Djdbc.level=3 ydoc.3

set JAVA_HOME=c:\jdk1.6.0_03
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin
call java -version

call ant -Djdbc.level=4 ydoc.4

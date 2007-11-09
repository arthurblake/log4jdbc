@echo off
setlocal

rem this is a bit convoluted because two jdk's must be invoked...

set ANT_HOME=c:\apache-ant-1.6.5

set JAVA_HOME=C:\j2sdk1.4.2_13
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin
call java -version

call ant -Djdbc.level=3 all

set JAVA_HOME=C:\jdk1.6.0_03
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin
call java -version

call ant -Djdbc.level=4 all

@echo off
setlocal

SET ANT_HOME=C:\JavaDev\apache-ant-1.10.12
set ANT_OPTS=-Xmx512m

SET JAVA_HOME=C:\JavaDev\jdk8u332-b09
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin

call java -version
call ant -Djdbc.level=4 -Djvm.ver=1.8 javadoc

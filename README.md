This fork adds 2 configurations:
`log4jdbc.sqltiming.usemarkersfortimingreports` - when set to true uses markers to log the timing of the queries. Default value is false.
`log4jdbc.dump.sql.reportoriginal` - when set to true, keeps the original sql query without replacing the ? signs

Also adds a static property `Slf4jSpyLogDelegator.markerFactory` to enable for custom marker creations for plugins such as [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) that have their own marker objects.

# log4jdbc
A more extensive README will be created soon. For now, you can view the usage instructions at the old Google Code hosting site:

https://code.google.com/p/log4jdbc/

You can download the prebuilt jars at:

https://code.google.com/p/log4jdbc/downloads/list

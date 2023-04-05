# How do I use log4jdbc with a data source?

At this time log4jdbc does not have direct support for data sources. It is one of the top features I would like to add at some point.

There is an alternative way to use log4jdbc that has worked really well for some people using data sources:

If you have a central location in your code where your application obtains connections, you can simply wrap the Connection object returned. Example:

```
// get connection from datasource
Connection conn = dataSource.getConnection();

// wrap the connection with log4jdbc
conn = new net.sf.log4jdbc.ConnectionSpy(conn);

// now use Connection as normal (but it will be audited by log4jdbc)
```

Since log4jdbc is being used in code instead of through the wrapper driver, it has the extra benefit of not requiring the log4jdbc driver to be initialized and the jdbc URL doesn't need to be changed either.


# What about Maven support?

Another feature, I plan to add at some point.  I just haven't had time.
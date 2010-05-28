/**
 * Copyright 2007-2010 Arthur Blake
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.log4jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * A JDBC driver which is a facade that delegates to one or more real underlying
 * JDBC drivers.  The driver will spy on any other JDBC driver that is loaded,
 * simply by prepending <code>jdbc:log4</code> to the normal jdbc driver URL
 * used by any other JDBC driver. The driver, by default, also loads several 
 * well known drivers at class load time, so that this driver can be 
 * "dropped in" to any Java program that uses these drivers without making any 
 * code changes.
 * <p/>
 * The well known driver classes that are loaded are:
 * <p/>
 * <p/>
 * <code>
 * <ul>
 * <li>oracle.jdbc.driver.OracleDriver</li>
 * <li>com.sybase.jdbc2.jdbc.SybDriver</li>
 * <li>net.sourceforge.jtds.jdbc.Driver</li>
 * <li>com.microsoft.jdbc.sqlserver.SQLServerDriver</li>
 * <li>com.microsoft.sqlserver.jdbc.SQLServerDriver</li>
 * <li>weblogic.jdbc.sqlserver.SQLServerDriver</li>
 * <li>com.informix.jdbc.IfxDriver</li>
 * <li>org.apache.derby.jdbc.ClientDriver</li>
 * <li>org.apache.derby.jdbc.EmbeddedDriver</li>
 * <li>com.mysql.jdbc.Driver</li>
 * <li>org.postgresql.Driver</li>
 * <li>org.hsqldb.jdbcDriver</li>
 * <li>org.h2.Driver</li>
 * </ul>
 * </code>
 * <p/>
 * <p/>
 * Additional drivers can be set via a property: <b>log4jdbc.drivers</b>
 * This can be either a single driver class name or a list of comma separated
 * driver class names.
 * <p/>
 * The autoloading behavior can be disabled by setting a property:
 * <b>log4jdbc.auto.load.popular.drivers</b> to false.  If that is done, then
 * the only drivers that log4jdbc will attempt to load are the ones specified
 * in <b>log4jdbc.drivers</b>.
 * <p/>
 * If any of the above driver classes cannot be loaded, the driver continues on
 * without failing.
 * <p/>
 * Note that the <code>getMajorVersion</code>, <code>getMinorVersion</code> and
 * <code>jdbcCompliant</code> method calls attempt to delegate to the last
 * underlying driver requested through any other call that accepts a JDBC URL.
 * <p/>
 * This can cause unexpected behavior in certain circumstances.  For example,
 * if one of these 3 methods is called before any underlying driver has been
 * established, then they will return default values that might not be correct
 * in all situations.  Similarly, if this spy driver is used to spy on more than
 * one underlying driver concurrently, the values returned by these 3 method
 * calls may change depending on what the last underlying driver used was at the
 * time.  This will not usually be a problem, since the driver is retrieved by
 * it's URL from the DriverManager in the first place (thus establishing an
 * underlying real driver), and in most applications their is only one database.
 *
 * @author Arthur Blake
 */
public class DriverSpy implements Driver
{
  /**
   * The last actual, underlying driver that was requested via a URL.
   */
  private Driver lastUnderlyingDriverRequested;

  /**
   * Maps driver class names to RdbmsSpecifics objects for each kind of
   * database.
   */
  private static Map rdbmsSpecifics;

  static final SpyLogDelegator log = SpyLogFactory.getSpyLogDelegator();

  /**
   * Optional package prefix to use for finding application generating point of
   * SQL.
   */
  static String DebugStackPrefix;

  /**
   * Flag to indicate debug trace info should be from the calling application
   * point of view (true if DebugStackPrefix is set.)
   */
  static boolean TraceFromApplication;

  /**
   * Flag to indicate if a warning should be shown if SQL takes more than
   * SqlTimingWarnThresholdMsec milliseconds to run.  See below.
   */
  static boolean SqlTimingWarnThresholdEnabled;

  /**
   * An amount of time in milliseconds for which SQL that executed taking this
   * long or more to run shall cause a warning message to be generated on the
   * SQL timing logger.
   *
   * This threshold will <i>ONLY</i> be used if SqlTimingWarnThresholdEnabled
   * is true.
   */
  static long SqlTimingWarnThresholdMsec;

  /**
   * Flag to indicate if an error should be shown if SQL takes more than
   * SqlTimingErrorThresholdMsec milliseconds to run.  See below.
   */
  static boolean SqlTimingErrorThresholdEnabled;

  /**
   * An amount of time in milliseconds for which SQL that executed taking this
   * long or more to run shall cause an error message to be generated on the
   * SQL timing logger.
   *
   * This threshold will <i>ONLY</i> be used if SqlTimingErrorThresholdEnabled
   * is true.
   */
  static long SqlTimingErrorThresholdMsec;

  /**
   * When dumping boolean values, dump them as 'true' or 'false'.
   * If this option is not set, they will be dumped as 1 or 0 as many
   * databases do not have a boolean type, and this allows for more
   * portable sql dumping.
   */
  static boolean DumpBooleanAsTrueFalse;

  /**
   * When dumping SQL, if this is greater than 0, than the SQL will
   * be broken up into lines that are no longer than this value.
   */
  static int DumpSqlMaxLineLength;

  /**
   * If this is true, display a special warning in the log along with the SQL
   * when the application uses a Statement (as opposed to a PreparedStatement.)
   * Using Statements for frequently used SQL can sometimes result in
   * performance and/or security problems.
   */
  static boolean StatementUsageWarn;

  /**
   * Options to more finely control which types of SQL statements will
   * be dumped, when dumping SQL.
   * By default all 5 of the following will be true.  If any one is set to
   * false, then that particular type of SQL will not be dumped.
   */
  static boolean DumpSqlSelect;
  static boolean DumpSqlInsert;
  static boolean DumpSqlUpdate;
  static boolean DumpSqlDelete;
  static boolean DumpSqlCreate;

  // only true if one ore more of the above 4 flags are false.
  static boolean DumpSqlFilteringOn;

  /**
   * If true, add a semilcolon to the end of each SQL dump.
   */
  static boolean DumpSqlAddSemicolon;

  /**
   * If dumping in debug mode, dump the full stack trace.
   * This will result in a VERY voluminous output, but can be very useful
   * under some circumstances.
   */
  static boolean DumpFullDebugStackTrace;
  
  /**
   * Attempt to Automatically load a set of popular JDBC drivers?
   */
  static boolean AutoLoadPopularDrivers;

  /**
   * Trim SQL before logging it?
   */
  static boolean TrimSql;

  /**
   * Coldfusion typically calls PreparedStatement.getGeneratedKeys() after
   * every SQL update call, even if it's not warranted.  This typically produces
   * an exception that is ignored by Coldfusion.  If this flag is true, then
   * any exception generated by this method is also ignored by log4jdbc.
   */
  static boolean SuppressGetGeneratedKeysException;

  /**
   * Get a Long option from a property and
   * log a debug message about this.
   *
   * @param props Properties to get option from.
   * @param propName property key.
   *
   * @return the value of that property key, converted
   * to a Long.  Or null if not defined or is invalid.
   */
  private static Long getLongOption(Properties props, String propName)
  {
    String propValue = props.getProperty(propName);
    Long longPropValue = null;
    if (propValue == null)
    {
      log.debug("x " + propName + " is not defined");
    }
    else
    {
      try
      {
        longPropValue = new Long(Long.parseLong(propValue));
        log.debug("  " + propName + " = " + longPropValue);
      }
      catch (NumberFormatException n)
      {
        log.debug("x " + propName + " \"" + propValue  +
          "\" is not a valid number");
      }
    }
    return longPropValue;
  }

  /**
   * Get a Long option from a property and
   * log a debug message about this.
   * 
   * @param props Properties to get option from.
   * @param propName property key.
   *
   * @return the value of that property key, converted
   * to a Long.  Or null if not defined or is invalid.
   */
  private static Long getLongOption(Properties props, String propName, 
    long defaultValue)
  {
    String propValue = props.getProperty(propName);
    Long longPropValue;
    if (propValue == null)
    {
      log.debug("x " + propName + " is not defined (using default of " + 
        defaultValue +")");
      longPropValue = new Long(defaultValue);
    }
    else
    {
      try
      {
        longPropValue = new Long(Long.parseLong(propValue));
        log.debug("  " + propName + " = " + longPropValue);
      }
      catch (NumberFormatException n)
      {
        log.debug("x " + propName + " \"" + propValue  +
          "\" is not a valid number (using default of " + defaultValue +")");
        longPropValue = new Long(defaultValue);
      }
    }
    return longPropValue;
  }

  /**
   * Get a String option from a property and
   * log a debug message about this.
   *
   * @param props Properties to get option from.
   * @param propName property key.
   * @return the value of that property key.
   */
  private static String getStringOption(Properties props, String propName)
  {
    String propValue = props.getProperty(propName);
    if (propValue == null || propValue.length()==0)
    {
      log.debug("x " + propName + " is not defined");
      propValue = null; // force to null, even if empty String
    }
    else
    {
      log.debug("  " + propName + " = " + propValue);
    }
    return propValue;
  }

  /**
   * Get a boolean option from a property and
   * log a debug message about this.
   * 
   * @param props Properties to get option from.
   * @param propName property name to get.
   * @param defaultValue default value to use if undefined.
   *
   * @return boolean value found in property, or defaultValue if no property
   *         found.
   */
  private static boolean getBooleanOption(Properties props, String propName,
    boolean defaultValue)
  {
    String propValue = props.getProperty(propName);
    boolean val;
    if (propValue == null)
    {
      log.debug("x " + propName + " is not defined (using default value " +
        defaultValue + ")");
      return defaultValue;
    }
    else
    {
      propValue = propValue.trim().toLowerCase();
      if (propValue.length() == 0)
      {
        val = defaultValue;
      }
      else
      {
        val= "true".equals(propValue) ||
          "yes".equals(propValue) || "on".equals(propValue);
      }
    }
    log.debug("  " + propName + " = " + val);
    return val;
  }

  static
  {
    log.debug("... log4jdbc initializing ...");

    InputStream propStream = 
      DriverSpy.class.getResourceAsStream("/log4jdbc.properties");
    
    Properties props = new Properties(System.getProperties());
    if (propStream != null)
    {
      try
      {
        props.load(propStream);
      }
      catch (IOException e)
      {
        log.debug("ERROR!  io exception loading " +
          "log4jdbc.properties from classpath: " + e.getMessage());
      }
      finally
      {
        try
        {
          propStream.close();
        }
        catch (IOException e)
        {
          log.debug("ERROR!  io exception closing property file stream: " + 
            e.getMessage());
				}
      }
      log.debug("  log4jdbc.properties loaded from classpath");
    }
    else
    {
      log.debug("  log4jdbc.properties not found on classpath");
    }

    // look for additional driver specified in properties
    DebugStackPrefix = getStringOption(props, "log4jdbc.debug.stack.prefix");
    TraceFromApplication = DebugStackPrefix != null;

    Long thresh = getLongOption(props, "log4jdbc.sqltiming.warn.threshold");
    SqlTimingWarnThresholdEnabled = (thresh != null);
    if (SqlTimingWarnThresholdEnabled)
    {
      SqlTimingWarnThresholdMsec = thresh.longValue();
    }

    thresh = getLongOption(props, "log4jdbc.sqltiming.error.threshold");
    SqlTimingErrorThresholdEnabled = (thresh != null);
    if (SqlTimingErrorThresholdEnabled)
    {
      SqlTimingErrorThresholdMsec = thresh.longValue();
    }

    DumpBooleanAsTrueFalse =
      getBooleanOption(props, "log4jdbc.dump.booleanastruefalse",false);

    DumpSqlMaxLineLength = getLongOption(props, 
      "log4jdbc.dump.sql.maxlinelength", 90L).intValue();

    DumpFullDebugStackTrace =
      getBooleanOption(props, "log4jdbc.dump.fulldebugstacktrace",false);

    StatementUsageWarn =
      getBooleanOption(props, "log4jdbc.statement.warn",false);

    DumpSqlSelect = getBooleanOption(props, "log4jdbc.dump.sql.select",true);
    DumpSqlInsert = getBooleanOption(props, "log4jdbc.dump.sql.insert",true);
    DumpSqlUpdate = getBooleanOption(props, "log4jdbc.dump.sql.update",true);
    DumpSqlDelete = getBooleanOption(props, "log4jdbc.dump.sql.delete",true);
    DumpSqlCreate = getBooleanOption(props, "log4jdbc.dump.sql.create",true);

    DumpSqlFilteringOn = !(DumpSqlSelect && DumpSqlInsert && DumpSqlUpdate &&
      DumpSqlDelete && DumpSqlCreate);

    DumpSqlAddSemicolon = getBooleanOption(props,
      "log4jdbc.dump.sql.addsemicolon", false);

    AutoLoadPopularDrivers = getBooleanOption(props,
      "log4jdbc.auto.load.popular.drivers", true);

    TrimSql = getBooleanOption(props, "log4jdbc.trim.sql", true);

    SuppressGetGeneratedKeysException = 
      getBooleanOption(props, "log4jdbc.suppress.generated.keys.exception", 
      false);

    // The Set of drivers that the log4jdbc driver will preload at instantiation
    // time.  The driver can spy on any driver type, it's just a little bit
    // easier to configure log4jdbc if it's one of these types!

    Set subDrivers = new TreeSet();

    if (AutoLoadPopularDrivers)
    {
      subDrivers.add("oracle.jdbc.driver.OracleDriver");
      subDrivers.add("oracle.jdbc.OracleDriver");
      subDrivers.add("com.sybase.jdbc2.jdbc.SybDriver");
      subDrivers.add("net.sourceforge.jtds.jdbc.Driver");

      // MS driver for Sql Server 2000
      subDrivers.add("com.microsoft.jdbc.sqlserver.SQLServerDriver");

      // MS driver for Sql Server 2005
      subDrivers.add("com.microsoft.sqlserver.jdbc.SQLServerDriver");

      subDrivers.add("weblogic.jdbc.sqlserver.SQLServerDriver");
      subDrivers.add("com.informix.jdbc.IfxDriver");
      subDrivers.add("org.apache.derby.jdbc.ClientDriver");
      subDrivers.add("org.apache.derby.jdbc.EmbeddedDriver");
      subDrivers.add("com.mysql.jdbc.Driver");
      subDrivers.add("org.postgresql.Driver");
      subDrivers.add("org.hsqldb.jdbcDriver");
      subDrivers.add("org.h2.Driver");
    }

    // look for additional driver specified in properties
    String moreDrivers = getStringOption(props, "log4jdbc.drivers");

    if (moreDrivers != null)
    {
      String[] moreDriversArr = moreDrivers.split(",");

      for (int i = 0; i < moreDriversArr.length; i++)
      {
        subDrivers.add(moreDriversArr[i]);
        log.debug ("    will look for specific driver " + moreDriversArr[i]);
      }
    }

    try
    {
      DriverManager.registerDriver(new DriverSpy());
    }
    catch (SQLException s)
    {
      // this exception should never be thrown, JDBC just defines it
      // for completeness
      throw (RuntimeException) new RuntimeException
        ("could not register log4jdbc driver!").initCause(s);
    }

    // instantiate all the supported drivers and remove
    // those not found
    String driverClass;
    for (Iterator i = subDrivers.iterator(); i.hasNext();)
    {
      driverClass = (String) i.next();
      try
      {
        Class.forName(driverClass);
        log.debug("  FOUND DRIVER " + driverClass);
      }
      catch (Throwable c)
      {
        i.remove();
      }
    }

    if (subDrivers.size() == 0)
    {
      log.debug("WARNING!  " +
        "log4jdbc couldn't find any underlying jdbc drivers.");
    }

    SqlServerRdbmsSpecifics sqlServer = new SqlServerRdbmsSpecifics();
    OracleRdbmsSpecifics oracle = new OracleRdbmsSpecifics();

    /** create lookup Map for specific rdbms formatters */
    rdbmsSpecifics = new HashMap();
    rdbmsSpecifics.put("oracle.jdbc.driver.OracleDriver", oracle);
    rdbmsSpecifics.put("oracle.jdbc.OracleDriver", oracle);
    rdbmsSpecifics.put("net.sourceforge.jtds.jdbc.Driver", sqlServer);
    rdbmsSpecifics.put("com.microsoft.jdbc.sqlserver.SQLServerDriver",
      sqlServer);
    rdbmsSpecifics.put("weblogic.jdbc.sqlserver.SQLServerDriver", sqlServer);

    log.debug("... log4jdbc initialized! ...");
  }

  static RdbmsSpecifics defaultRdbmsSpecifics = new RdbmsSpecifics();

  /**
   * Get the RdbmsSpecifics object for a given Connection.
   *
   * @param conn JDBC connection to get RdbmsSpecifics for.
   * @return RdbmsSpecifics for the given connection.
   */
  static RdbmsSpecifics getRdbmsSpecifics(Connection conn)
  {
    String driverName = "";
    try
    {
      DatabaseMetaData dbm = conn.getMetaData();
      driverName = dbm.getDriverName();
    }
    catch (SQLException s)
    {
      // silently fail
    }

    log.debug("driver name is " + driverName);

    RdbmsSpecifics r = (RdbmsSpecifics) rdbmsSpecifics.get(driverName);

    if (r == null)
    {
      return defaultRdbmsSpecifics;
    }
    else
    {
      return r;
    }
  }

  /**
   * Default constructor.
   */
  public DriverSpy()
  {
  }

  /**
   * Get the major version of the driver.  This call will be delegated to the
   * underlying driver that is being spied upon (if there is no underlying
   * driver found, then 1 will be returned.)
   *
   * @return the major version of the JDBC driver.
   */
  public int getMajorVersion()
  {
    if (lastUnderlyingDriverRequested == null)
    {
      return 1;
    }
    else
    {
      return lastUnderlyingDriverRequested.getMajorVersion();
    }
  }

  /**
   * Get the minor version of the driver.  This call will be delegated to the
   * underlying driver that is being spied upon (if there is no underlying
   * driver found, then 0 will be returned.)
   *
   * @return the minor version of the JDBC driver.
   */
  public int getMinorVersion()
  {
    if (lastUnderlyingDriverRequested == null)
    {
      return 0;
    }
    else
    {
      return lastUnderlyingDriverRequested.getMinorVersion();
    }
  }

  /**
   * Report whether the underlying driver is JDBC compliant.  If there is no
   * underlying driver, false will be returned, because the driver cannot
   * actually do any work without an underlying driver.
   *
   * @return <code>true</code> if the underlying driver is JDBC Compliant;
   *         <code>false</code> otherwise.
   */
  public boolean jdbcCompliant()
  {
    return lastUnderlyingDriverRequested != null &&
      lastUnderlyingDriverRequested.jdbcCompliant();
  }

  /**
   * Returns true if this is a <code>jdbc:log4</code> URL and if the URL is for
   * an underlying driver that this DriverSpy can spy on.
   *
   * @param url JDBC URL.
   *
   * @return true if this Driver can handle the URL.
   *
   * @throws SQLException if a database access error occurs
   */
  public boolean acceptsURL(String url) throws SQLException
  {
    Driver d = getUnderlyingDriver(url);
    if (d != null)
    {
      lastUnderlyingDriverRequested = d;
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * Given a <code>jdbc:log4</code> type URL, find the underlying real driver
   * that accepts the URL.
   *
   * @param url JDBC connection URL.
   *
   * @return Underlying driver for the given URL. Null is returned if the URL is
   *         not a <code>jdbc:log4</code> type URL or there is no underlying
   *         driver that accepts the URL.
   *
   * @throws SQLException if a database access error occurs.
   */
  private Driver getUnderlyingDriver(String url) throws SQLException
  {
    if (url.startsWith("jdbc:log4"))
    {
      url = url.substring(9);

      Enumeration e = DriverManager.getDrivers();

      Driver d;
      while (e.hasMoreElements())
      {
        d = (Driver) e.nextElement();

        if (d.acceptsURL(url))
        {
          return d;
        }
      }
    }
    return null;
  }

  /**
   * Get a Connection to the database from the underlying driver that this
   * DriverSpy is spying on.  If logging is not enabled, an actual Connection to
   * the database returned.  If logging is enabled, a ConnectionSpy object which
   * wraps the real Connection is returned.
   *
   * @param url  JDBC connection URL
   * .
   * @param info a list of arbitrary string tag/value pairs as
   *             connection arguments. Normally at least a "user" and
   *             "password" property should be included.
   *
   * @return     a <code>Connection</code> object that represents a
   *             connection to the URL.
   *
   * @throws SQLException if a database access error occurs
   */
  public Connection connect(String url, Properties info) throws SQLException
  {
    Driver d = getUnderlyingDriver(url);
    if (d == null)
    {
      return null;
    }

    // get actual URL that the real driver expects
    // (strip off "jdbc:log4" from url)
    url = url.substring(9);

    lastUnderlyingDriverRequested = d;
    Connection c = d.connect(url, info);

    if (c == null)
    {
      throw new SQLException("invalid or unknown driver url: " + url);
    }
    if (log.isJdbcLoggingEnabled())
    {
      ConnectionSpy cspy = new ConnectionSpy(c);
      RdbmsSpecifics r = null;
      String dclass = d.getClass().getName();
      if (dclass != null && dclass.length() > 0)
      {
        r = (RdbmsSpecifics) rdbmsSpecifics.get(dclass);
      }

      if (r == null)
      {
        r = defaultRdbmsSpecifics;
      }
      cspy.setRdbmsSpecifics(r);
      return cspy;
    }
    else
    {
      return c;
    }
  }

  /**
   * Gets information about the possible properties for the underlying driver.
   *
   * @param url  the URL of the database to which to connect
   *
   * @param info a proposed list of tag/value pairs that will be sent on
   *             connect open
   * @return     an array of <code>DriverPropertyInfo</code> objects describing
   *             possible properties.  This array may be an empty array if no
   *             properties are required.
   *
   * @throws SQLException if a database access error occurs
   */
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
    throws SQLException
  {
    Driver d = getUnderlyingDriver(url);
    if (d == null)
    {
      return new DriverPropertyInfo[0];
    }

    lastUnderlyingDriverRequested = d;
    return d.getPropertyInfo(url, info);
  }
}

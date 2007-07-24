/**
 * Copyright 2007 Arthur Blake
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
 * A JDBC driver which is a facade that delegates to one or more real underlying JDBC drivers.
 * The driver will spy on any other JDBC driver that is loaded, simply by prepending <code>jdbc:log4</code>
 * to the normal jdbc driver URL used by any other JDBC driver. The driver also loads several well known drivers at
 * class load time, so that this driver can be "dropped in" to any java program that uses these drivers
 * without making any code changes.  The well known driver classes that are loaded are:
 * <p/>
 * <code>
 * <ul>
 * <p/>
 * <li>oracle.jdbc.driver.OracleDriver</li>
 * <li>com.sybase.jdbc2.jdbc.SybDriver</li>
 * <li>net.sourceforge.jtds.jdbc.Driver</li>
 * <li>com.microsoft.jdbc.sqlserver.SQLServerDriver</li>
 * <li>weblogic.jdbc.sqlserver.SQLServerDriver</li>
 * <li>com.informix.jdbc.IfxDriver</li>
 * <li>org.apache.derby.jdbc.ClientDriver</li>
 * <li>org.apache.derby.jdbc.EmbeddedDriver</li>
 * <p/>
 * </ul>
 * </code>
 * <p/>
 * Additional drivers can be set via a system property: <b>log4jdbc.drivers</b>
 * This can be either a single driver class name or a list of comma separated driver class names.
 * <p/>
 * If any of the above driver classes cannot be loaded, the driver continues on without failing.
 * <p/>
 * Note that the <code>getMajorVersion</code>, <code>getMinorVersion</code> and <code>jdbcCompliant</code>
 * method calls attempt to delegate to the last underlying driver requested through any other call that
 * accepts a JDBC URL.
 * <p/>
 * This can cause unexpected behavior in certain circumstances.  For example, if one of these
 * 3 methods is called before any underlying driver has been established, then they will return
 * default values that might not be correct in all situations.  Similarly, if this spy driver
 * is used to spy on more than one underlying driver concurrently, the values returned by these
 * 3 method calls may change depending on what the last underlying driver used was at the time.
 * This will not usually be a problem, since the driver is retrieved by it's URL from the DriverManager
 * in the first place (thus establishing an underlying real driver), and in most applications
 * their is only one database.
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
   * Maps driver class names to RdbmsSpecifics objects for each kind of database.
   */
  private static Map rdbmsSpecifics;

  static final SpyLogDelegator log = SpyLogFactory.getSpyLogDelegator();

  /**
   * Optional package prefix to use for finding application generating point of SQL.
   */
  static final String DebugStackPrefix;

  /**
   * Flag to indicate debug trace info should be from app point of view (true if DebugStackPrefix is set.)
   */
  static final boolean TraceFromApplication;

  static
  {
    log.debug(" log4jdbc initializing...");

    // look for additional driver specified in system properties
    DebugStackPrefix = System.getProperty("log4jdbc.debug.stack.prefix");
    TraceFromApplication = DebugStackPrefix != null && DebugStackPrefix.length() > 0;

    if (TraceFromApplication)
    {
      log.debug(" debug entry point prefix is \"" + DebugStackPrefix + "\"");
    }

    // the Set of drivers that this driver spy will preload at instantiation time
    // the driver will spy on any driver

    Set subDrivers = new TreeSet();
    subDrivers.add("oracle.jdbc.driver.OracleDriver");
    subDrivers.add("com.sybase.jdbc2.jdbc.SybDriver");
    subDrivers.add("net.sourceforge.jtds.jdbc.Driver");
    subDrivers.add("com.microsoft.jdbc.sqlserver.SQLServerDriver");
    subDrivers.add("weblogic.jdbc.sqlserver.SQLServerDriver");
    subDrivers.add("com.informix.jdbc.IfxDriver");
    subDrivers.add("org.apache.derby.jdbc.ClientDriver");
    subDrivers.add("org.apache.derby.jdbc.EmbeddedDriver");

    // look for additional driver specified in system properties
    String moreDrivers = System.getProperty("log4jdbc.drivers");

    if (moreDrivers != null && moreDrivers.length() > 0)
    {
      String[] moreDriversArr = moreDrivers.split(",");

      for (int i = 0; i < moreDriversArr.length; i++)
      {
        subDrivers.add(moreDriversArr[i]);
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
      throw (RuntimeException) new RuntimeException("could not register log4jdbc driver!").initCause(s);
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
        log.debug("FOUND " + driverClass);
      }
      catch (ClassNotFoundException c)
      {
//        log.debug(" ... (not found) ..." + driverClass);
        i.remove();
      }
    }

    if (subDrivers.size() == 0)
    {
      log.debug("WARNING!  log4jdbc couldn't find any underlying jdbc drivers.");
    }

    SqlServerRdbmsSpecifics sqlServer = new SqlServerRdbmsSpecifics();

    /** create lookup Map for specific rdbms formatters */
    rdbmsSpecifics = new HashMap();
    rdbmsSpecifics.put("oracle.jdbc.driver.OracleDriver", new OracleRdbmsSpecifics());
    rdbmsSpecifics.put("net.sourceforge.jtds.jdbc.Driver", sqlServer);
    rdbmsSpecifics.put("com.microsoft.jdbc.sqlserver.SQLServerDriver", sqlServer);
    rdbmsSpecifics.put("weblogic.jdbc.sqlserver.SQLServerDriver", sqlServer);

    log.debug(" ... log4jdbc initialized!");
  }

  static RdbmsSpecifics defaultRdbmsSpecifics = new RdbmsSpecifics();

  /**
   * Get the RdbmsSpecifics object for a given Connection.
   *
   * @param conn jdbc connection to get RdbmsSpecifics for.
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
   * Get the major version of the driver.
   * This call will be delegated to the underlying driver that is being spied upon.
   * (if there is no underlying driver found, then 1 will be returned.)
   *
   * @return the major version of the jdbc driver.
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
   * Get the minor version of the driver.
   * This call will be delegated to the underlying driver that is being spied upon.
   * (if there is no underlying driver found, then 0 will be returned.)
   *
   * @return the minor version of the jdbc driver.
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
   * Report whether the underlying driver is jdbcClient.  If there is no underlying driver, false
   * will be returned, because the driver cannot actually do any work without an underlying driver.
   *
   * @return <code>true</code> if the underlying driver is JDBC Compliant; <code>false</code>
   *         otherwise.
   */
  public boolean jdbcCompliant()
  {
    return lastUnderlyingDriverRequested != null && lastUnderlyingDriverRequested.jdbcCompliant();
  }

  /**
   * Returns true if this is a <code>jdbc:log4</code> url and if the url is for an underlying driver
   * that this DriverSpy can spy on.
   *
   * @param url jdbc url.
   * @return true if this Driver can handle the url.
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
   * @param url jdbc connection url.
   * @return Underlying driver for the given url. Null is returned if the url is not a <code>jdbc:log4</code> type URL
   *         or there is no underlying driver that accepts the URL.
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
   * DriverSpy is spying on.
   * <p/>
   * If logging is not enabled, an actual Connection to the database returned.
   * If logging is enabled, a ConnectionSpy object which wraps the real Connection
   * is returned.
   *
   * @param url  jdbc connection url.
   * @param info a list of arbitrary string tag/value pairs as
   *             connection arguments. Normally at least a "user" and
   *             "password" property should be included.
   * @return a <code>Connection</code> object that represents a
   *         connection to the URL.
   * @throws SQLException if a database access error occurs
   */
  public Connection connect(String url, Properties info) throws SQLException
  {
    Driver d = getUnderlyingDriver(url);
    if (d == null)
    {
      return null;
    }

    url = url.substring(9); // get actual URL that the real driver expects (strip off "jdbc:log4" from url)

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
   * @param info a proposed list of tag/value pairs that will be sent on
   *             connect open
   * @return an array of <code>DriverPropertyInfo</code> objects describing
   *         possible properties.  This array may be an empty array if
   *         no properties are required.
   * @throws SQLException if a database access error occurs
   */
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
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

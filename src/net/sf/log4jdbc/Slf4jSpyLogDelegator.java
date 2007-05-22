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

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Delegates JDBC spy logging events to the the Simple Logging Facade for Java (slf4j).
 *
 * @author Arthur Blake
 */
public class Slf4jSpyLogDelegator implements SpyLogDelegator
{
  /**
   * Create a SpyLogDelegator specific to the Simple Logging Facade for Java (slf4j).
   */
  public Slf4jSpyLogDelegator()
  {
  }

  // logs for sql and jdbc

  /**
   * Logger that shows all JDBC calls on INFO level (exception ResultSet calls)
   */
  public static final Logger jdbcLogger = LoggerFactory.getLogger("jdbc.audit");

  /**
   * Logger that shows JDBC calls for ResultSet operations
   */
  public static final Logger resultSetLogger = LoggerFactory.getLogger("jdbc.resultset");

  /**
   * Logger that shows only the SQL that is occuring
   */
  public static final Logger sqlOnlyLogger = LoggerFactory.getLogger("jdbc.sqlonly");

  /**
   * Logger that shows the SQL timing, post execution
   */
  public static final Logger sqlTimingLogger = LoggerFactory.getLogger("jdbc.sqltiming");

  // admin/setup logging for log4jdbc.

  /**
   * Logger just for debugging things within log4jdbc itself (admin, setup, etc.)
   */
  public static final Logger debugLogger = LoggerFactory.getLogger("log4jdbc.debug");

  /**
   * Determine if any of the spy loggers are turned on.
   *
   * @return true if any of the spy jdbc/sql loggers are enabled at error level or higher.
   */
  public boolean isJdbcLoggingEnabled()
  {


    //todo: how can we handle this better??
    return true;
/*
    return jdbcLogger.isEnabledFor(Level.ERROR) || resultSetLogger.isEnabledFor(Level.ERROR) ||
      sqlOnlyLogger.isEnabledFor(Level.ERROR) || sqlTimingLogger.isEnabledFor(Level.ERROR);
*/
  }

  /**
   * Called when a jdbc method throws an Exception.
   *
   * @param spy        the Spy wrapping the class that threw an Exception.
   * @param methodCall a description of the name and call parameters of the method generated the Exception.
   * @param e          the Exception that was thrown.
   * @param sql        optional sql that occured just before the exception occured.
   * @param execTime   optional amount of time that passed before an exception was thrown when sql was being executed.
   *                   caller should pass -1 if not used
   */
  public void exceptionOccured(Spy spy, String methodCall, Exception e, String sql, long execTime)
  {
    String classType = spy.getClassType();
    int spyNo = spy.getConnectionNumber();
    String header = spyNo + ". " + classType + "." + methodCall;
    if (sql == null)
    {
      jdbcLogger.error(header, e);
      sqlOnlyLogger.error(header, e);
    }
    else
    {
      jdbcLogger.error(header + " " + sql, e);
      sqlOnlyLogger.error(header + " " + sql, e);
      sqlTimingLogger.error(header + " FAILED! " + sql + " {FAILED after " + execTime + " msec}", e);
    }
  }

  /**
   * Called when a jdbc method from a Connection, Statement, PreparedStatement, CallableStatement or ResultSet
   * returns.
   *
   * @param spy        the Spy wrapping the class that called the method that returned.
   * @param methodCall a description of the name and call parameters of the method that returned.
   * @param returnMsg  return value converted to a String for integral types, or String representation for Object
   *                   return types this will be null for void return types.
   */
  public void methodReturned(Spy spy, String methodCall, String returnMsg)
  {
    String classType = spy.getClassType();
    int spyNo = spy.getConnectionNumber();
    if (ResultSetSpy.classTypeDescription.equals(classType))
    {
      if (resultSetLogger.isDebugEnabled())
      {
        resultSetLogger.debug(
          spyNo + ". " + classType + "." + methodCall + " returned " + returnMsg + " " + getDebugInfo());
      }
      else if (resultSetLogger.isInfoEnabled())
      {
        resultSetLogger.info(spyNo + ". " + classType + "." + methodCall + " returned " + returnMsg);
      }
    }
    else
    {
      if (jdbcLogger.isDebugEnabled())
      {
        jdbcLogger.debug(
          spyNo + ". " + classType + "." + methodCall + " returned " + returnMsg + " " + getDebugInfo());
      }
      else if (jdbcLogger.isInfoEnabled())
      {
        jdbcLogger.info(spyNo + ". " + classType + "." + methodCall + " returned " + returnMsg);
      }
    }
  }

  /**
   * Called when a spied upon object is constructed.
   *
   * @param spy              the Spy wrapping the class that called the method that returned.
   * @param constructionInfo information about the object construction
   */
  public void constructorReturned(Spy spy, String constructionInfo)
  {
    // not used in this implementation -- yet
  }


  private static String nl = System.getProperty("line.separator");

  /**
   * Special call that is called only for JDBC method calls that contain SQL.
   *
   * @param spy        the Spy wrapping the class where the SQL occured.
   * @param methodCall a description of the name and call parameters of the method that generated the SQL.
   * @param sql        sql that occured.
   */
  public void sqlOccured(Spy spy, String methodCall, String sql)
  {
    int spyNo = spy.getConnectionNumber();

    if (sqlOnlyLogger.isDebugEnabled())
    {
      sqlOnlyLogger.debug(getDebugInfo() + nl + spyNo + ". " + sql);
    }
    else if (sqlOnlyLogger.isInfoEnabled())
    {
      sqlOnlyLogger.info(sql);
    }
  }

  /**
   * Special call that is called only for JDBC method calls that contain SQL.
   *
   * @param spy        the    Spy wrapping the class where the SQL occured.
   * @param execTime   how long it took the sql to run, in msec.
   * @param methodCall a description of the name and call parameters of the method that generated the SQL.
   * @param sql        sql that occured.
   */
  public void sqlTimingOccured(Spy spy, long execTime, String methodCall, String sql)
  {
    int spyNo = spy.getConnectionNumber();

    if (sqlTimingLogger.isDebugEnabled())
    {
      sqlTimingLogger.debug(getDebugInfo() + nl + spyNo + ". " + sql + " {executed in " + execTime + " msec}");
    }
    else if (sqlTimingLogger.isInfoEnabled())
    {
      sqlTimingLogger.info(sql + " {executed in " + execTime + " msec}");
    }
  }

  /**
   * Get debugging info - the module and line number that called the logger
   * version that prints the stack trace information from the point just before we got it (net.sf.log4jdbc)
   *
   * if the optional log4jdbc.debug.stack.prefix system property is defined then
   * the last call point from an application is shown in the debug
   * trace output, instead of the last direct caller into log4jdbc
   *
   * @return debugging info for whoever called into jdbc from within the application.
   */
  private static String getDebugInfo()
  {
    Throwable t = new Throwable();
    t.fillInStackTrace();
    StackTraceElement[] stackTrace = t.getStackTrace();

    if (stackTrace != null)
    {
      int firstLog4jdbcCall = 0;
      int lastApplicationCall = 0;

      String className;
      for (int i = 0; i < stackTrace.length; i++)
      {
        className = stackTrace[i].getClassName();
        if (className.startsWith("net.sf.log4jdbc"))
        {
          firstLog4jdbcCall = i;
        }
        else if (DriverSpy.TraceFromApplication &&
          className.startsWith(DriverSpy.DebugStackPrefix))
        {
          lastApplicationCall = i;
          break;
        }
      }

      StringBuffer dump = new StringBuffer(" ");
      int j = lastApplicationCall;

      if (j == 0)  // if app not found, then use whoever was the last guy that called a log4jdbc class.
      {
        j = 1 + firstLog4jdbcCall;
      }

      dump.append(stackTrace[j].getClassName()).append(".").append(stackTrace[j].getMethodName()).append("(").
        append(stackTrace[j].getFileName()).append(":").append(stackTrace[j].getLineNumber()).append(")");

      return dump.toString();
    }
    else
    {
      return null;
    }
  }

  /**
   * Log a Setup and/or administrative log message for log4jdbc.
   *
   * @param msg message to log.
   */
  public void debug(String msg)
  {
    debugLogger.debug(msg);
  }
}


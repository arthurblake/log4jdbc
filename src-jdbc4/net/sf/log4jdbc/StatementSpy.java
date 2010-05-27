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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

/**
 * Wraps a Statement and reports method calls, returns and exceptions.
 *
 * jdbc 4 version
 *
 * @author Arthur Blake
 */
public class StatementSpy implements Statement, Spy
{
  protected final SpyLogDelegator log;

  /**
   * The Connection that created this Statement.
   */
  protected ConnectionSpy connectionSpy;

  /**
   * The real statement that this StatementSpy wraps.
   */
  protected Statement realStatement;

  /**
   * Get the real Statement that this StatementSpy wraps.
   *
   * @return the real Statement that this StatementSpy wraps.
   */
  public Statement getRealStatement()
  {
    return realStatement;
  }

  /**
   * Create a StatementSpy that wraps another Statement
   * for the purpose of logging all method calls, sql, exceptions and return values.
   *
   * @param connectionSpy Connection that created this Statement.
   * @param realStatement real underlying Statement that this StatementSpy wraps.
   */
  public StatementSpy(ConnectionSpy connectionSpy, Statement realStatement)
  {
    if (realStatement == null)
    {
      throw new IllegalArgumentException("Must pass in a non null real Statement");
    }
    if (connectionSpy == null)
    {
      throw new IllegalArgumentException("Must pass in a non null ConnectionSpy");
    }
    this.realStatement = realStatement;
    this.connectionSpy = connectionSpy;

    log = SpyLogFactory.getSpyLogDelegator();

    if (realStatement instanceof CallableStatement)
    {
      reportReturn("new CallableStatement");
    }
    else if (realStatement instanceof PreparedStatement)
    {
      reportReturn("new PreparedStatement");
    }
    else
    {
      reportReturn("new Statement");
    }
  }

  public String getClassType()
  {
    return "Statement";
  }

  public Integer getConnectionNumber()
  {
    return connectionSpy.getConnectionNumber();
  }

  /**
   * Report an exception to be logged which includes timing data on a sql failure.
   * @param methodCall description of method call and arguments passed to it that generated the exception.
   * @param exception exception that was generated
   * @param sql SQL associated with the call.
   * @param execTime amount of time that the jdbc driver was chugging on the SQL before it threw an exception.
   */
  protected void reportException(String methodCall, SQLException exception, String sql, long execTime)
  {
    log.exceptionOccured(this, methodCall, exception, sql, execTime);
  }

  /**
   * Report an exception to be logged.
   * @param methodCall description of method call and arguments passed to it that generated the exception.
   * @param exception exception that was generated
   * @param sql SQL associated with the call.
   */
  protected void reportException(String methodCall, SQLException exception, String sql)
  {
    log.exceptionOccured(this, methodCall, exception, sql, -1L);
  }

  /**
   * Report an exception to be logged.
   *
   * @param methodCall description of method call and arguments passed to it that generated the exception.
   * @param exception exception that was generated
   */
  protected void reportException(String methodCall, SQLException exception)
  {
    log.exceptionOccured(this, methodCall, exception, null, -1L);
  }

  /**
   * Report (for logging) that a method returned.  All the other reportReturn methods are conveniance methods that call this method.
   *
   * @param methodCall description of method call and arguments passed to it that returned.
   * @param msg description of what the return value that was returned.  may be an empty String for void return types.
   */
  protected void reportAllReturns(String methodCall, String msg)
  {
    log.methodReturned(this, methodCall, msg);
  }

  /**
   * Conveniance method to report (for logging) that a method returned a boolean value.
   *
   * @param methodCall description of method call and arguments passed to it that returned.
   * @param value boolean return value.
   * @return the boolean return value as passed in.
   */
  protected boolean reportReturn(String methodCall, boolean value)
  {
    reportAllReturns(methodCall, "" + value);
    return value;
  }

  /**
   * Conveniance method to report (for logging) that a method returned a byte value.
   *
   * @param methodCall description of method call and arguments passed to it that returned.
   * @param value byte return value.
   * @return the byte return value as passed in.
   */
  protected byte reportReturn(String methodCall, byte value)
  {
    reportAllReturns(methodCall, "" + value);
    return value;
  }

  /**
   * Conveniance method to report (for logging) that a method returned a int value.
   *
   * @param methodCall description of method call and arguments passed to it that returned.
   * @param value int return value.
   * @return the int return value as passed in.
   */
  protected int reportReturn(String methodCall, int value)
  {
    reportAllReturns(methodCall, "" + value);
    return value;
  }

  /**
   * Conveniance method to report (for logging) that a method returned a double value.
   *
   * @param methodCall description of method call and arguments passed to it that returned.
   * @param value double return value.
   * @return the double return value as passed in.
   */
  protected double reportReturn(String methodCall, double value)
  {
    reportAllReturns(methodCall, "" + value);
    return value;
  }

  /**
   * Conveniance method to report (for logging) that a method returned a short value.
   *
   * @param methodCall description of method call and arguments passed to it that returned.
   * @param value short return value.
   * @return the short return value as passed in.
   */
  protected short reportReturn(String methodCall, short value)
  {
    reportAllReturns(methodCall, "" + value);
    return value;
  }

  /**
   * Conveniance method to report (for logging) that a method returned a long value.
   *
   * @param methodCall description of method call and arguments passed to it that returned.
   * @param value long return value.
   * @return the long return value as passed in.
   */
  protected long reportReturn(String methodCall, long value)
  {
    reportAllReturns(methodCall, "" + value);
    return value;
  }

  /**
   * Conveniance method to report (for logging) that a method returned a float value.
   *
   * @param methodCall description of method call and arguments passed to it that returned.
   * @param value float return value.
   * @return the float return value as passed in.
   */
  protected float reportReturn(String methodCall, float value)
  {
    reportAllReturns(methodCall, "" + value);
    return value;
  }

  /**
   * Conveniance method to report (for logging) that a method returned an Object.
   *
   * @param methodCall description of method call and arguments passed to it that returned.
   * @param value return Object.
   * @return the return Object as passed in.
   */
  protected Object reportReturn(String methodCall, Object value)
  {
    reportAllReturns(methodCall, "" + value);
    return value;
  }

  /**
   * Conveniance method to report (for logging) that a method returned (void return type).
   *
   * @param methodCall description of method call and arguments passed to it that returned.
   */
  protected void reportReturn(String methodCall)
  {
    reportAllReturns(methodCall, "");
  }

  /**
   * Running one-off statement sql is generally inefficient and a bad idea for various reasons,
   * so give a warning when this is done.
   */
  private static final String StatementSqlWarning = "{WARNING: Statement used to run SQL} ";

  /**
   * Report SQL for logging with a warning that it was generated from a statement.
   *
   * @param sql        the SQL being run
   * @param methodCall the name of the method that was running the SQL
   */
  protected void reportStatementSql(String sql, String methodCall)
  {
    // redirect to one more method call ONLY so that stack trace search is consistent
    // with the reportReturn calls
    _reportSql((DriverSpy.StatementUsageWarn?StatementSqlWarning:"") +
      sql, methodCall);
  }

  /**
   * Report SQL for logging with a warning that it was generated from a statement.
   *
   * @param execTime   execution time in msec.
   * @param sql        the SQL being run
   * @param methodCall the name of the method that was running the SQL
   */
  protected void reportStatementSqlTiming(long execTime, String sql, String methodCall)
  {
    // redirect to one more method call ONLY so that stack trace search is consistent
    // with the reportReturn calls
    _reportSqlTiming(execTime, (DriverSpy.StatementUsageWarn?StatementSqlWarning:"") +
      sql, methodCall);
  }

  /**
   * Report SQL for logging.
   *
   * @param execTime   execution time in msec.
   * @param sql        the SQL being run
   * @param methodCall the name of the method that was running the SQL
   */
  protected void reportSqlTiming(long execTime, String sql, String methodCall)
  {
    // redirect to one more method call ONLY so that stack trace search is consistent
    // with the reportReturn calls
    _reportSqlTiming(execTime, sql, methodCall);
  }

  /**
   * Report SQL for logging.
   *
   * @param sql        the SQL being run
   * @param methodCall the name of the method that was running the SQL
   */
  protected void reportSql(String sql, String methodCall)
  {
    // redirect to one more method call ONLY so that stack trace search is consistent
    // with the reportReturn calls
    _reportSql(sql, methodCall);
  }

  private void _reportSql(String sql, String methodCall)
  {
    log.sqlOccured(this, methodCall, sql);
  }

  private void _reportSqlTiming(long execTime, String sql, String methodCall)
  {
    log.sqlTimingOccured(this, execTime, methodCall, sql);
  }

  // implementation of interface methods
  public SQLWarning getWarnings() throws SQLException
  {
    String methodCall = "getWarnings()";
    try
    {
      return (SQLWarning) reportReturn(methodCall, realStatement.getWarnings());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public int executeUpdate(String sql, String[] columnNames) throws SQLException
  {
    String methodCall = "executeUpdate(" + sql + ", " + columnNames + ")";
    reportStatementSql(sql, methodCall);
    long tstart = System.currentTimeMillis();
    try
    {
      int result = realStatement.executeUpdate(sql, columnNames);
      reportStatementSqlTiming(System.currentTimeMillis() - tstart, sql, methodCall);
      return reportReturn(methodCall, result);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s, sql, System.currentTimeMillis() - tstart);
      throw s;
    }
  }

  public boolean execute(String sql, String[] columnNames) throws SQLException
  {
    String methodCall = "execute(" + sql + ", " + columnNames + ")";
    reportStatementSql(sql, methodCall);
    long tstart = System.currentTimeMillis();
    try
    {
      boolean result = realStatement.execute(sql, columnNames);
      reportStatementSqlTiming(System.currentTimeMillis() - tstart, sql, methodCall);
      return reportReturn(methodCall, result);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s, sql, System.currentTimeMillis() - tstart);
      throw s;
    }
  }

  public void setMaxRows(int max) throws SQLException
  {
    String methodCall = "setMaxRows(" + max + ")";
    try
    {
      realStatement.setMaxRows(max);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public boolean getMoreResults() throws SQLException
  {
    String methodCall = "getMoreResults()";

    try
    {
      return reportReturn(methodCall, realStatement.getMoreResults());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void clearWarnings() throws SQLException
  {
    String methodCall = "clearWarnings()";
    try
    {
      realStatement.clearWarnings();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  /**
   * Tracking of current batch (see addBatch, clearBatch and executeBatch)
   * //todo: should access to this List be synchronized?
   */
  protected List currentBatch = new ArrayList();

  public void addBatch(String sql) throws SQLException
  {
    String methodCall = "addBatch(" + sql + ")";

    currentBatch.add(StatementSqlWarning + sql);
    try
    {
      realStatement.addBatch(sql);
    }
    catch (SQLException s)
    {
      reportException(methodCall,s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public int getResultSetType() throws SQLException
  {
    String methodCall = "getResultSetType()";
    try
    {
      return reportReturn(methodCall, realStatement.getResultSetType());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void clearBatch() throws SQLException
  {
    String methodCall = "clearBatch()";
    try
    {
      realStatement.clearBatch();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    currentBatch.clear();
    reportReturn(methodCall);
  }

  public void setFetchDirection(int direction) throws SQLException
  {
    String methodCall = "setFetchDirection(" + direction + ")";
    try
    {
      realStatement.setFetchDirection(direction);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public int[] executeBatch() throws SQLException
  {
    String methodCall = "executeBatch()";

    int j=currentBatch.size();
    StringBuffer batchReport = new StringBuffer("batching " + j + " statements:");

    int fieldSize = (""+j).length();

    String sql;
    for (int i=0; i < j;)
    {
      sql = (String) currentBatch.get(i);
      batchReport.append("\n");
      batchReport.append(Utilities.rightJustify(fieldSize,""+(++i)));
      batchReport.append(":  ");
      batchReport.append(sql);
    }

    sql = batchReport.toString();
    reportSql(sql, methodCall);
    long tstart = System.currentTimeMillis();

    int[] updateResults;
    try
    {
      updateResults = realStatement.executeBatch();
      reportSqlTiming(System.currentTimeMillis()-tstart, sql, methodCall);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s, sql, System.currentTimeMillis()-tstart);
      throw s;
    }
    currentBatch.clear();
    return (int[])reportReturn(methodCall,updateResults);
  }

  public void setFetchSize(int rows) throws SQLException
  {
    String methodCall = "setFetchSize(" + rows + ")";
    try
    {
      realStatement.setFetchSize(rows);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public int getQueryTimeout() throws SQLException
  {
    String methodCall = "getQueryTimeout()";
    try
    {
      return reportReturn(methodCall, realStatement.getQueryTimeout());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Connection getConnection() throws SQLException
  {
    String methodCall = "getConnection()";
    return (Connection) reportReturn(methodCall, connectionSpy);
  }

  public ResultSet getGeneratedKeys() throws SQLException
  {
    String methodCall = "getGeneratedKeys()";
    try
    {
      ResultSet r = realStatement.getGeneratedKeys();
      if (r == null)
      {
        return (ResultSet) reportReturn(methodCall, r);
      }
      else
      {
        return (ResultSet) reportReturn(methodCall, new ResultSetSpy(this, r));
      }
    }
    catch (SQLException s)
    {
      if (!DriverSpy.SuppressGetGeneratedKeysException)
      {
        reportException(methodCall, s);
      }
      throw s;
    }
  }

  public void setEscapeProcessing(boolean enable) throws SQLException
  {
    String methodCall = "setEscapeProcessing(" + enable + ")";
    try
    {
      realStatement.setEscapeProcessing(enable);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public int getFetchDirection() throws SQLException
  {
    String methodCall = "getFetchDirection()";
    try
    {
      return reportReturn(methodCall, realStatement.getFetchDirection());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void setQueryTimeout(int seconds) throws SQLException
  {
    String methodCall = "setQueryTimeout(" + seconds + ")";
    try
    {
      realStatement.setQueryTimeout(seconds);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public boolean getMoreResults(int current) throws SQLException
  {
    String methodCall = "getMoreResults(" + current + ")";

    try
    {
      return reportReturn(methodCall, realStatement.getMoreResults(current));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public ResultSet executeQuery(String sql) throws SQLException
  {
    String methodCall = "executeQuery(" + sql + ")";
    reportStatementSql(sql, methodCall);
    long tstart = System.currentTimeMillis();
    try
    {
      ResultSet result = realStatement.executeQuery(sql);
      reportStatementSqlTiming(System.currentTimeMillis() - tstart, sql, methodCall);
      ResultSetSpy r = new ResultSetSpy(this, result);
      return (ResultSet) reportReturn(methodCall, r);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s, sql, System.currentTimeMillis() - tstart);
      throw s;
    }
  }

  public int getMaxFieldSize() throws SQLException
  {
    String methodCall = "getMaxFieldSize()";
    try
    {
      return reportReturn(methodCall, realStatement.getMaxFieldSize());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public int executeUpdate(String sql) throws SQLException
  {
    String methodCall = "executeUpdate(" + sql + ")";
    reportStatementSql(sql, methodCall);
    long tstart = System.currentTimeMillis();
    try
    {
      int result = realStatement.executeUpdate(sql);
      reportStatementSqlTiming(System.currentTimeMillis() - tstart, sql, methodCall);
      return reportReturn(methodCall, result);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s, sql, System.currentTimeMillis() - tstart);
      throw s;
    }
  }

  public void cancel() throws SQLException
  {
    String methodCall = "cancel()";
    try
    {
      realStatement.cancel();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void setCursorName(String name) throws SQLException
  {
    String methodCall = "setCursorName(" + name + ")";
    try
    {
      realStatement.setCursorName(name);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public int getFetchSize() throws SQLException
  {
    String methodCall = "getFetchSize()";
    try
    {
      return reportReturn(methodCall, realStatement.getFetchSize());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public int getResultSetConcurrency() throws SQLException
  {
    String methodCall = "getResultSetConcurrency()";
    try
    {
      return reportReturn(methodCall, realStatement.getResultSetConcurrency());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public int getResultSetHoldability() throws SQLException
  {
    String methodCall = "getResultSetHoldability()";
    try
    {
      return reportReturn(methodCall, realStatement.getResultSetHoldability());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean isClosed() throws SQLException {
    String methodCall = "isClosed()";
    try
    {
      return reportReturn(methodCall, realStatement.isClosed());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void setPoolable(boolean poolable) throws SQLException {
    String methodCall = "setPoolable(" + poolable + ")";
    try
    {
      realStatement.setPoolable(poolable);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public boolean isPoolable() throws SQLException {
    String methodCall = "isPoolable()";
    try
    {
      return reportReturn(methodCall, realStatement.isPoolable());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void setMaxFieldSize(int max) throws SQLException
  {
    String methodCall = "setMaxFieldSize(" + max + ")";
    try
    {
      realStatement.setMaxFieldSize(max);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public boolean execute(String sql) throws SQLException
  {
    String methodCall = "execute(" + sql + ")";
    reportStatementSql(sql, methodCall);
    long tstart = System.currentTimeMillis();
    try
    {
      boolean result = realStatement.execute(sql);
      reportStatementSqlTiming(System.currentTimeMillis() - tstart, sql, methodCall);
      return reportReturn(methodCall, result);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s, sql, System.currentTimeMillis() - tstart);
      throw s;
    }
  }

  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
  {
    String methodCall = "executeUpdate(" + sql + ", " + autoGeneratedKeys + ")";
    reportStatementSql(sql, methodCall);
    long tstart = System.currentTimeMillis();
    try
    {
      int result = realStatement.executeUpdate(sql, autoGeneratedKeys);
      reportStatementSqlTiming(System.currentTimeMillis() - tstart, sql, methodCall);
      return reportReturn(methodCall, result);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s, sql, System.currentTimeMillis() - tstart);
      throw s;
    }
  }

  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
  {
    String methodCall = "execute(" + sql + ", " + autoGeneratedKeys + ")";
    reportStatementSql(sql, methodCall);
    long tstart = System.currentTimeMillis();
    try
    {
      boolean result = realStatement.execute(sql, autoGeneratedKeys);
      reportStatementSqlTiming(System.currentTimeMillis() - tstart, sql, methodCall);
      return reportReturn(methodCall, result);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s, sql, System.currentTimeMillis() - tstart);
      throw s;
    }
  }

  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
  {
    String methodCall = "executeUpdate(" + sql + ", " + columnIndexes + ")";
    reportStatementSql(sql, methodCall);
    long tstart = System.currentTimeMillis();
    try
    {
      int result = realStatement.executeUpdate(sql, columnIndexes);
      reportStatementSqlTiming(System.currentTimeMillis() - tstart, sql, methodCall);
      return reportReturn(methodCall, result);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s, sql, System.currentTimeMillis() - tstart);
      throw s;
    }
  }

  public boolean execute(String sql, int[] columnIndexes) throws SQLException
  {
    String methodCall = "execute(" + sql + ", " + columnIndexes + ")";
    reportStatementSql(sql, methodCall);
    long tstart = System.currentTimeMillis();
    try
    {
      boolean result = realStatement.execute(sql, columnIndexes);
      reportStatementSqlTiming(System.currentTimeMillis() - tstart, sql, methodCall);
      return reportReturn(methodCall, result);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s, sql, System.currentTimeMillis() - tstart);
      throw s;
    }
  }

  public ResultSet getResultSet() throws SQLException
  {
    String methodCall = "getResultSet()";
    try
    {
      ResultSet r = realStatement.getResultSet();
      if (r == null)
      {
        return (ResultSet) reportReturn(methodCall, r);
      }
      else
      {
        return (ResultSet) reportReturn(methodCall, new ResultSetSpy(this, r));
      }
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public int getMaxRows() throws SQLException
  {
    String methodCall = "getMaxRows()";
    try
    {
      return reportReturn(methodCall, realStatement.getMaxRows());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void close() throws SQLException
  {
    String methodCall = "close()";
    try
    {
      realStatement.close();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public int getUpdateCount() throws SQLException
  {
    String methodCall = "getUpdateCount()";
    try
    {
      return reportReturn(methodCall, realStatement.getUpdateCount());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    String methodCall = "unwrap(" + (iface==null?"null":iface.getName()) + ")";
    try
    {
      //todo: double check this logic
      return (T)reportReturn(methodCall, (iface != null && (iface == Connection.class || iface == Spy.class))?(T)this:realStatement.unwrap(iface));
    }
    catch (SQLException s)
    {
      reportException(methodCall,s);
      throw s;
    }
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException
  {
    String methodCall = "isWrapperFor(" + (iface==null?"null":iface.getName()) + ")";
    try
    {
      return reportReturn(methodCall, (iface != null && (iface == Statement.class || iface == Spy.class)) ||
          realStatement.isWrapperFor(iface));
    }
    catch (SQLException s)
    {
      reportException(methodCall,s);
      throw s;
    }
  }

}

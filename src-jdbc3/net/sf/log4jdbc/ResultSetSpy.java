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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * Wraps a ResultSet and reports method calls, returns and exceptions.
 *
 * JDBC 3 version.
 *
 * @author Arthur Blake
 */
public class ResultSetSpy implements ResultSet, Spy
{
  private final SpyLogDelegator log;

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
   * Report (for logging) that a method returned.  All the other reportReturn methods are conveniance methods that call
   * this method.
   *
   * @param methodCall description of method call and arguments passed to it that returned.
   * @param msg description of what the return value that was returned.  may be an empty String for void return types.
   */
  protected void reportAllReturns(String methodCall, String msg)
  {
    log.methodReturned(this, methodCall, msg);
  }

  private ResultSet realResultSet;

  /**
   * Get the real ResultSet that this ResultSetSpy wraps.
   *
   * @return the real ResultSet that this ResultSetSpy wraps.
   */
  public ResultSet getRealResultSet()
  {
    return realResultSet;
  }

  private StatementSpy parent;

  /**
   * Create a new ResultSetSpy that wraps another ResultSet object, that logs all method calls, expceptions, etc.
   *
   * @param parent Statement that generated this ResultSet.
   * @param realResultSet real underlying ResultSet that is being wrapped.
   */
  public ResultSetSpy(StatementSpy parent, ResultSet realResultSet)
  {
    if (realResultSet == null)
    {
      throw new IllegalArgumentException("Must provide a non null real ResultSet");
    }
    this.realResultSet = realResultSet;
    this.parent = parent;
    log = SpyLogFactory.getSpyLogDelegator();
    reportReturn("new ResultSet");
  }

  /**
   * Description for ResultSet class type.
   */
  public static final String classTypeDescription = "ResultSet";

  public String getClassType()
  {
    return classTypeDescription;
  }

  public Integer getConnectionNumber()
  {
    return parent.getConnectionNumber();
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

  // forwarding methods

  public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException
  {
    String methodCall = "updateAsciiStream(" + columnIndex + ", " + x + ", " + length + ")";
    try
    {
      realResultSet.updateAsciiStream(columnIndex, x, length);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException
  {
    String methodCall = "updateAsciiStream(" + columnName + ", " + x + ", " + length + ")";
    try
    {
      realResultSet.updateAsciiStream(columnName, x, length);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public int getRow() throws SQLException
  {
    String methodCall = "getRow()";
    try
    {
      return reportReturn(methodCall, realResultSet.getRow());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void cancelRowUpdates() throws SQLException
  {
    String methodCall = "cancelRowUpdates()";
    try
    {
      realResultSet.cancelRowUpdates();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public Time getTime(int columnIndex) throws SQLException
  {
    String methodCall = "getTime(" + columnIndex + ")";
    try
    {
      return (Time) reportReturn(methodCall, realResultSet.getTime(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Time getTime(String columnName) throws SQLException
  {
    String methodCall = "getTime(" + columnName + ")";
    try
    {
      return (Time) reportReturn(methodCall, realResultSet.getTime(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Time getTime(int columnIndex, Calendar cal) throws SQLException
  {
    String methodCall = "getTime(" + columnIndex + ", " + cal + ")";
    try
    {
      return (Time) reportReturn(methodCall, realResultSet.getTime(columnIndex, cal));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Time getTime(String columnName, Calendar cal) throws SQLException
  {
    String methodCall = "getTime(" + columnName + ", " + cal + ")";
    try
    {
      return (Time) reportReturn(methodCall, realResultSet.getTime(columnName, cal));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean absolute(int row) throws SQLException
  {
    String methodCall = "absolute(" + row + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.absolute(row));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Timestamp getTimestamp(int columnIndex) throws SQLException
  {
    String methodCall = "getTimestamp(" + columnIndex + ")";
    try
    {
      return (Timestamp) reportReturn(methodCall, realResultSet.getTimestamp(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Timestamp getTimestamp(String columnName) throws SQLException
  {
    String methodCall = "getTimestamp(" + columnName + ")";
    try
    {
      return (Timestamp) reportReturn(methodCall, realResultSet.getTimestamp(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }

  }

  public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException
  {
    String methodCall = "getTimestamp(" + columnIndex + ", " + cal + ")";
    try
    {
      return (Timestamp) reportReturn(methodCall, realResultSet.getTimestamp(columnIndex, cal));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }

  }

  public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException
  {
    String methodCall = "getTimestamp(" + columnName + ", " + cal + ")";
    try
    {
      return (Timestamp) reportReturn(methodCall, realResultSet.getTimestamp(columnName, cal));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void moveToInsertRow() throws SQLException
  {
    String methodCall = "moveToInsertRow()";
    try
    {
      realResultSet.moveToInsertRow();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public boolean relative(int rows) throws SQLException
  {
    String methodCall = "relative(" + rows + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.relative(rows));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean previous() throws SQLException
  {
    String methodCall = "previous()";
    try
    {
      return reportReturn(methodCall, realResultSet.previous());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void moveToCurrentRow() throws SQLException
  {
    String methodCall = "moveToCurrentRow()";
    try
    {
      realResultSet.moveToCurrentRow();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public Ref getRef(int i) throws SQLException
  {
    String methodCall = "getRef(" + i + ")";
    try
    {
      return (Ref) reportReturn(methodCall, realResultSet.getRef(i));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateRef(int columnIndex, Ref x) throws SQLException
  {
    String methodCall = "updateRef(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateRef(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public Ref getRef(String colName) throws SQLException
  {
    String methodCall = "getRef(" + colName + ")";
    try
    {
      return (Ref) reportReturn(methodCall, realResultSet.getRef(colName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateRef(String columnName, Ref x) throws SQLException
  {
    String methodCall = "updateRef(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateRef(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public Blob getBlob(int i) throws SQLException
  {
    String methodCall = "getBlob(" + i + ")";
    try
    {
      return (Blob) reportReturn(methodCall, realResultSet.getBlob(i));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateBlob(int columnIndex, Blob x) throws SQLException
  {
    String methodCall = "updateBlob(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateBlob(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public Blob getBlob(String colName) throws SQLException
  {
    String methodCall = "getBlob(" + colName + ")";
    try
    {
      return (Blob) reportReturn(methodCall, realResultSet.getBlob(colName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateBlob(String columnName, Blob x) throws SQLException
  {
    String methodCall = "updateBlob(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateBlob(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public Clob getClob(int i) throws SQLException
  {
    String methodCall = "getClob(" + i + ")";
    try
    {
      return (Clob) reportReturn(methodCall, realResultSet.getClob(i));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateClob(int columnIndex, Clob x) throws SQLException
  {
    String methodCall = "updateClob(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateClob(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public Clob getClob(String colName) throws SQLException
  {
    String methodCall = "getClob(" + colName + ")";
    try
    {
      return (Clob) reportReturn(methodCall, realResultSet.getClob(colName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateClob(String columnName, Clob x) throws SQLException
  {
    String methodCall = "updateClob(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateClob(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public boolean getBoolean(int columnIndex) throws SQLException
  {
    String methodCall = "getBoolean(" + columnIndex + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getBoolean(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean getBoolean(String columnName) throws SQLException
  {
    String methodCall = "getBoolean(" + columnName + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getBoolean(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Array getArray(int i) throws SQLException
  {
    String methodCall = "getArray(" + i + ")";
    try
    {
      return (Array) reportReturn(methodCall, realResultSet.getArray(i));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateArray(int columnIndex, Array x) throws SQLException
  {
    String methodCall = "updateArray(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateArray(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public Array getArray(String colName) throws SQLException
  {
    String methodCall = "getArray(" + colName + ")";
    try
    {
      return (Array) reportReturn(methodCall, realResultSet.getArray(colName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateArray(String columnName, Array x) throws SQLException
  {
    String methodCall = "updateArray(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateArray(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public boolean isBeforeFirst() throws SQLException
  {
    String methodCall = "isBeforeFirst()";
    try
    {
      return reportReturn(methodCall, realResultSet.isBeforeFirst());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public short getShort(int columnIndex) throws SQLException
  {
    String methodCall = "getShort(" + columnIndex + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getShort(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public short getShort(String columnName) throws SQLException
  {
    String methodCall = "getShort(" + columnName + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getShort(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public int getInt(int columnIndex) throws SQLException
  {
    String methodCall = "getInt(" + columnIndex + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getInt(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public int getInt(String columnName) throws SQLException
  {
    String methodCall = "getInt(" + columnName + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getInt(columnName));
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
      realResultSet.close();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public ResultSetMetaData getMetaData() throws SQLException
  {
    String methodCall = "getMetaData()";
    try
    {
      return (ResultSetMetaData) reportReturn(methodCall, realResultSet.getMetaData());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public int getType() throws SQLException
  {
    String methodCall = "getType()";
    try
    {
      return reportReturn(methodCall, realResultSet.getType());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public double getDouble(int columnIndex) throws SQLException
  {
    String methodCall = "getDouble(" + columnIndex + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getDouble(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public double getDouble(String columnName) throws SQLException
  {
    String methodCall = "getDouble(" + columnName + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getDouble(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void deleteRow() throws SQLException
  {
    String methodCall = "deleteRow()";
    try
    {
      realResultSet.deleteRow();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public int getConcurrency() throws SQLException
  {
    String methodCall = "getConcurrency()";
    try
    {
      return reportReturn(methodCall, realResultSet.getConcurrency());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean rowUpdated() throws SQLException
  {
    String methodCall = "rowUpdated()";
    try
    {
      return reportReturn(methodCall, realResultSet.rowUpdated());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Date getDate(int columnIndex) throws SQLException
  {
    String methodCall = "getDate(" + columnIndex + ")";
    try
    {
      return (Date) reportReturn(methodCall, realResultSet.getDate(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Date getDate(String columnName) throws SQLException
  {
    String methodCall = "getDate(" + columnName + ")";
    try
    {
      return (Date) reportReturn(methodCall, realResultSet.getDate(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Date getDate(int columnIndex, Calendar cal) throws SQLException
  {
    String methodCall = "getDate(" + columnIndex + ", " + cal + ")";
    try
    {
      return (Date) reportReturn(methodCall, realResultSet.getDate(columnIndex, cal));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }

  }

  public Date getDate(String columnName, Calendar cal) throws SQLException
  {
    String methodCall = "getDate(" + columnName + ", " + cal + ")";
    try
    {
      return (Date) reportReturn(methodCall, realResultSet.getDate(columnName, cal));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean last() throws SQLException
  {
    String methodCall = "last()";
    try
    {
      return reportReturn(methodCall, realResultSet.last());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean rowInserted() throws SQLException
  {
    String methodCall = "rowInserted()";
    try
    {
      return reportReturn(methodCall, realResultSet.rowInserted());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean rowDeleted() throws SQLException
  {
    String methodCall = "rowDeleted()";
    try
    {
      return reportReturn(methodCall, realResultSet.rowDeleted());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateNull(int columnIndex) throws SQLException
  {
    String methodCall = "updateNull(" + columnIndex + ")";
    try
    {
      realResultSet.updateNull(columnIndex);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateNull(String columnName) throws SQLException
  {
    String methodCall = "updateNull(" + columnName + ")";
    try
    {
      realResultSet.updateNull(columnName);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateShort(int columnIndex, short x) throws SQLException
  {
    String methodCall = "updateShort(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateShort(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateShort(String columnName, short x) throws SQLException
  {
    String methodCall = "updateShort(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateShort(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateBoolean(int columnIndex, boolean x) throws SQLException
  {
    String methodCall = "updateBoolean(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateBoolean(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateBoolean(String columnName, boolean x) throws SQLException
  {
    String methodCall = "updateBoolean(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateBoolean(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateByte(int columnIndex, byte x) throws SQLException
  {
    String methodCall = "updateByte(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateByte(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateByte(String columnName, byte x) throws SQLException
  {
    String methodCall = "updateByte(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateByte(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateInt(int columnIndex, int x) throws SQLException
  {
    String methodCall = "updateInt(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateInt(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateInt(String columnName, int x) throws SQLException
  {
    String methodCall = "updateInt(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateInt(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public Object getObject(int columnIndex) throws SQLException
  {
    String methodCall = "getObject(" + columnIndex + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getObject(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Object getObject(String columnName) throws SQLException
  {
    String methodCall = "getObject(" + columnName + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getObject(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Object getObject(int i, Map map) throws SQLException
  {
    String methodCall = "getObject(" + i + ", " + map + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getObject(i, map));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Object getObject(String colName, Map map) throws SQLException
  {
    String methodCall = "getObject(" + colName + ", " + map + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getObject(colName, map));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean next() throws SQLException
  {
    String methodCall = "next()";
    try
    {
      return reportReturn(methodCall, realResultSet.next());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateLong(int columnIndex, long x) throws SQLException
  {
    String methodCall = "updateLong(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateLong(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateLong(String columnName, long x) throws SQLException
  {
    String methodCall = "updateLong(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateLong(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateFloat(int columnIndex, float x) throws SQLException
  {
    String methodCall = "updateFloat(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateFloat(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);

  }

  public void updateFloat(String columnName, float x) throws SQLException
  {
    String methodCall = "updateFloat(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateFloat(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateDouble(int columnIndex, double x) throws SQLException
  {
    String methodCall = "updateDouble(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateDouble(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateDouble(String columnName, double x) throws SQLException
  {
    String methodCall = "updateDouble(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateDouble(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public Statement getStatement() throws SQLException
  {
    String methodCall = "getStatement()";
    try
    {
      Statement s = realResultSet.getStatement();
      if (s == null)
      {
        return (Statement) reportReturn(methodCall, s);
      }
      else
      {
        //todo: what's going on here?
        return (Statement) reportReturn(methodCall, new StatementSpy(new ConnectionSpy(s.getConnection()), s));
      }
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateString(int columnIndex, String x) throws SQLException
  {
    String methodCall = "updateString(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateString(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateString(String columnName, String x) throws SQLException
  {
    String methodCall = "updateString(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateString(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public InputStream getAsciiStream(int columnIndex) throws SQLException
  {
    String methodCall = "getAsciiStream(" + columnIndex + ")";
    try
    {
      return (InputStream) reportReturn(methodCall, realResultSet.getAsciiStream(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public InputStream getAsciiStream(String columnName) throws SQLException
  {
    String methodCall = "getAsciiStream(" + columnName + ")";
    try
    {
      return (InputStream) reportReturn(methodCall, realResultSet.getAsciiStream(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
  {
    String methodCall = "updateBigDecimal(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateBigDecimal(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public URL getURL(int columnIndex) throws SQLException
  {
    String methodCall = "getURL(" + columnIndex + ")";
    try
    {
      return (URL) reportReturn(methodCall, realResultSet.getURL(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException
  {
    String methodCall = "updateBigDecimal(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateBigDecimal(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public URL getURL(String columnName) throws SQLException
  {
    String methodCall = "getURL(" + columnName + ")";
    try
    {
      return (URL) reportReturn(methodCall, realResultSet.getURL(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateBytes(int columnIndex, byte[] x) throws SQLException
  {
    // todo: dump array?
    String methodCall = "updateBytes(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateBytes(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateBytes(String columnName, byte[] x) throws SQLException
  {
    // todo: dump array?
    String methodCall = "updateBytes(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateBytes(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  /**
   * @deprecated
   */
  public InputStream getUnicodeStream(int columnIndex) throws SQLException
  {
    String methodCall = "getUnicodeStream(" + columnIndex + ")";
    try
    {
      return (InputStream) reportReturn(methodCall, realResultSet.getUnicodeStream(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  /**
   * @deprecated
   */
  public InputStream getUnicodeStream(String columnName) throws SQLException
  {
    String methodCall = "getUnicodeStream(" + columnName + ")";
    try
    {
      return (InputStream) reportReturn(methodCall, realResultSet.getUnicodeStream(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateDate(int columnIndex, Date x) throws SQLException
  {
    String methodCall = "updateDate(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateDate(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateDate(String columnName, Date x) throws SQLException
  {
    String methodCall = "updateDate(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateDate(columnName, x);
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
      return reportReturn(methodCall, realResultSet.getFetchSize());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public SQLWarning getWarnings() throws SQLException
  {
    String methodCall = "getWarnings()";
    try
    {
      return (SQLWarning) reportReturn(methodCall, realResultSet.getWarnings());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public InputStream getBinaryStream(int columnIndex) throws SQLException
  {
    String methodCall = "getBinaryStream(" + columnIndex + ")";
    try
    {
      return (InputStream) reportReturn(methodCall, realResultSet.getBinaryStream(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public InputStream getBinaryStream(String columnName) throws SQLException
  {
    String methodCall = "getBinaryStream(" + columnName + ")";
    try
    {
      return (InputStream) reportReturn(methodCall, realResultSet.getBinaryStream(columnName));
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
      realResultSet.clearWarnings();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException
  {
    String methodCall = "updateTimestamp(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateTimestamp(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateTimestamp(String columnName, Timestamp x) throws SQLException
  {
    String methodCall = "updateTimestamp(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateTimestamp(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public boolean first() throws SQLException
  {
    String methodCall = "first()";
    try
    {
      return reportReturn(methodCall, realResultSet.first());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public String getCursorName() throws SQLException
  {
    String methodCall = "getCursorName()";
    try
    {
      return (String) reportReturn(methodCall, realResultSet.getCursorName());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public int findColumn(String columnName) throws SQLException
  {
    String methodCall = "findColumn(" + columnName + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.findColumn(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean wasNull() throws SQLException
  {
    String methodCall = "wasNull()";
    try
    {
      return reportReturn(methodCall, realResultSet.wasNull());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException
  {
    String methodCall = "updateBinaryStream(" + columnIndex + ", " + x + ", " + length + ")";
    try
    {
      realResultSet.updateBinaryStream(columnIndex, x, length);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException
  {
    String methodCall = "updateBinaryStream(" + columnName + ", " + x + ", " + length + ")";
    try
    {
      realResultSet.updateBinaryStream(columnName, x, length);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public String getString(int columnIndex) throws SQLException
  {
    String methodCall = "getString(" + columnIndex + ")";
    try
    {
      return (String) reportReturn(methodCall, realResultSet.getString(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public String getString(String columnName) throws SQLException
  {
    String methodCall = "getString(" + columnName + ")";
    try
    {
      return (String) reportReturn(methodCall, realResultSet.getString(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Reader getCharacterStream(int columnIndex) throws SQLException
  {
    String methodCall = "getCharacterStream(" + columnIndex + ")";
    try
    {
      return (Reader) reportReturn(methodCall, realResultSet.getCharacterStream(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public Reader getCharacterStream(String columnName) throws SQLException
  {
    String methodCall = "getCharacterStream(" + columnName + ")";
    try
    {
      return (Reader) reportReturn(methodCall, realResultSet.getCharacterStream(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void setFetchDirection(int direction) throws SQLException
  {
    String methodCall = "setFetchDirection(" + direction + ")";
    try
    {
      realResultSet.setFetchDirection(direction);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException
  {
    String methodCall = "updateCharacterStream(" + columnIndex + ", " + x + ", " + length + ")";
    try
    {
      realResultSet.updateCharacterStream(columnIndex, x, length);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException
  {
    String methodCall = "updateCharacterStream(" + columnName + ", " + reader + ", " + length + ")";
    try
    {
      realResultSet.updateCharacterStream(columnName, reader, length);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public byte getByte(int columnIndex) throws SQLException
  {
    String methodCall = "getByte(" + columnIndex + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getByte(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public byte getByte(String columnName) throws SQLException
  {
    String methodCall = "getByte(" + columnName + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getByte(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateTime(int columnIndex, Time x) throws SQLException
  {
    String methodCall = "updateTime(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateTime(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateTime(String columnName, Time x) throws SQLException
  {
    String methodCall = "updateTime(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateTime(columnName, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public byte[] getBytes(int columnIndex) throws SQLException
  {
    String methodCall = "getBytes(" + columnIndex + ")";
    try
    {
      return (byte[]) reportReturn(methodCall, realResultSet.getBytes(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public byte[] getBytes(String columnName) throws SQLException
  {
    String methodCall = "getBytes(" + columnName + ")";
    try
    {
      return (byte[]) reportReturn(methodCall, realResultSet.getBytes(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean isAfterLast() throws SQLException
  {
    String methodCall = "isAfterLast()";
    try
    {
      return reportReturn(methodCall, realResultSet.isAfterLast());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void updateObject(int columnIndex, Object x, int scale) throws SQLException
  {
    String methodCall = "updateObject(" + columnIndex + ", " + x + ", " + scale + ")";
    try
    {
      realResultSet.updateObject(columnIndex, x, scale);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateObject(int columnIndex, Object x) throws SQLException
  {
    String methodCall = "updateObject(" + columnIndex + ", " + x + ")";
    try
    {
      realResultSet.updateObject(columnIndex, x);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateObject(String columnName, Object x, int scale) throws SQLException
  {
    String methodCall = "updateObject(" + columnName + ", " + x + ", " + scale + ")";
    try
    {
      realResultSet.updateObject(columnName, x, scale);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateObject(String columnName, Object x) throws SQLException
  {
    String methodCall = "updateObject(" + columnName + ", " + x + ")";
    try
    {
      realResultSet.updateObject(columnName, x);
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
      return reportReturn(methodCall, realResultSet.getFetchDirection());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public long getLong(int columnIndex) throws SQLException
  {
    String methodCall = "getLong(" + columnIndex + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getLong(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public long getLong(String columnName) throws SQLException
  {
    String methodCall = "getLong(" + columnName + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getLong(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean isFirst() throws SQLException
  {
    String methodCall = "isFirst()";
    try
    {
      return reportReturn(methodCall, realResultSet.isFirst());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void insertRow() throws SQLException
  {
    String methodCall = "insertRow()";
    try
    {
      realResultSet.insertRow();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public float getFloat(int columnIndex) throws SQLException
  {
    String methodCall = "getFloat(" + columnIndex + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getFloat(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public float getFloat(String columnName) throws SQLException
  {
    String methodCall = "getFloat(" + columnName + ")";
    try
    {
      return reportReturn(methodCall, realResultSet.getFloat(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public boolean isLast() throws SQLException
  {
    String methodCall = "isLast()";
    try
    {
      return reportReturn(methodCall, realResultSet.isLast());
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void setFetchSize(int rows) throws SQLException
  {
    String methodCall = "setFetchSize(" + rows + ")";
    try
    {
      realResultSet.setFetchSize(rows);
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void updateRow() throws SQLException
  {
    String methodCall = "updateRow()";
    try
    {
      realResultSet.updateRow();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void beforeFirst() throws SQLException
  {
    String methodCall = "beforeFirst()";
    try
    {
      realResultSet.beforeFirst();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  /**
   * @deprecated
   */
  public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
  {
    String methodCall = "getBigDecimal(" + columnIndex + ", " + scale + ")";
    try
    {
      return (BigDecimal) reportReturn(methodCall, realResultSet.getBigDecimal(columnIndex, scale));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  /**
   * @deprecated
   */
  public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException
  {
    String methodCall = "getBigDecimal(" + columnName + ", " + scale + ")";
    try
    {
      return (BigDecimal) reportReturn(methodCall, realResultSet.getBigDecimal(columnName, scale));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public BigDecimal getBigDecimal(int columnIndex) throws SQLException
  {
    String methodCall = "getBigDecimal(" + columnIndex + ")";
    try
    {
      return (BigDecimal) reportReturn(methodCall, realResultSet.getBigDecimal(columnIndex));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public BigDecimal getBigDecimal(String columnName) throws SQLException
  {
    String methodCall = "getBigDecimal(" + columnName + ")";
    try
    {
      return (BigDecimal) reportReturn(methodCall, realResultSet.getBigDecimal(columnName));
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }

  public void afterLast() throws SQLException
  {
    String methodCall = "afterLast()";
    try
    {
      realResultSet.afterLast();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
    reportReturn(methodCall);
  }

  public void refreshRow() throws SQLException
  {
    String methodCall = "refreshRow()";
    try
    {
      realResultSet.refreshRow();
    }
    catch (SQLException s)
    {
      reportException(methodCall, s);
      throw s;
    }
  }
}

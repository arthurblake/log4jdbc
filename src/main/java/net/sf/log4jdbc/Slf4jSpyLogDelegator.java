/**
 * Copyright 2007-2015 Arthur Blake
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
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegates JDBC spy logging events to the the Simple Logging Facade for Java
 * (slf4j).
 * 
 * @author Arthur Blake
 */
public class Slf4jSpyLogDelegator implements SpyLogDelegator
{
	/**
	 * Create a SpyLogDelegator specific to the Simple Logging Facade for Java
	 * (slf4j).
	 */
	public Slf4jSpyLogDelegator()
	{
	}

	// logs for sql and jdbc

	/**
	 * Logger that shows all JDBC calls on INFO level (exception ResultSet calls)
	 */
	private final Logger jdbcLogger = LoggerFactory.getLogger("jdbc.audit");

	/**
	 * Logger that shows JDBC calls for ResultSet operations
	 */
	private final Logger resultSetLogger = LoggerFactory
		.getLogger("jdbc.resultset");

	/**
	 * Logger that shows only the SQL that is occuring
	 */
	private final Logger sqlOnlyLogger = LoggerFactory.getLogger("jdbc.sqlonly");

	/**
	 * Logger that shows the SQL timing, post execution
	 */
	private final Logger sqlTimingLogger = LoggerFactory
		.getLogger("jdbc.sqltiming");

	/**
	 * Logger that shows connection open and close events as well as current
	 * number of open connections.
	 */
	private final Logger connectionLogger = LoggerFactory
		.getLogger("jdbc.connection");

	// admin/setup logging for log4jdbc.

	/**
	 * Logger just for debugging things within log4jdbc itself (admin, setup,
	 * etc.)
	 */
	private final Logger debugLogger = LoggerFactory.getLogger("log4jdbc.debug");

	/**
	 * Determine if any of the 5 log4jdbc spy loggers are turned on (jdbc.audit |
	 * jdbc.resultset | jdbc.sqlonly | jdbc.sqltiming | jdbc.connection)
	 * 
	 * @return true if any of the 5 spy jdbc/sql loggers are enabled at debug info
	 *         or error level.
	 */
	public boolean isJdbcLoggingEnabled()
	{
		return jdbcLogger.isErrorEnabled() || resultSetLogger.isErrorEnabled() ||
			sqlOnlyLogger.isErrorEnabled() || sqlTimingLogger.isErrorEnabled() ||
			connectionLogger.isErrorEnabled();
	}

	/**
	 * Called when a jdbc method throws an Exception.
	 * 
	 * @param spy the Spy wrapping the class that threw an Exception.
	 * @param methodCall a description of the name and call parameters of the
	 *        method generated the Exception.
	 * @param e the Exception that was thrown.
	 * @param sql optional sql that occured just before the exception occured.
	 * @param execTime optional amount of time that passed before an exception was
	 *        thrown when sql was being executed. caller should pass -1 if not
	 *        used
	 */
	public void exceptionOccured(Spy spy, String methodCall, Exception e,
		String sql, long execTime)
	{
		String classType = spy.getClassType();
		Integer spyNo = spy.getConnectionNumber();
		String header = spyNo + ". " + classType + "." + methodCall;
		if (sql == null)
		{
			jdbcLogger.error(header, e);
			sqlOnlyLogger.error(header, e);
			sqlTimingLogger.error(header, e);
		}
		else
		{
			sql = processSql(sql);
			jdbcLogger.error(header + " " + sql, e);

			// if at debug level, display debug info to error log
			if (sqlOnlyLogger.isDebugEnabled())
			{
				sqlOnlyLogger.error(getDebugInfo() + nl + spyNo + ". " + sql, e);
			}
			else
			{
				sqlOnlyLogger.error(header + " " + sql, e);
			}

			// if at debug level, display debug info to error log
			if (sqlTimingLogger.isDebugEnabled())
			{
				sqlTimingLogger.error(getDebugInfo() + nl + spyNo + ". " + sql +
					" {FAILED after " + execTime + " msec}", e);
			}
			else
			{
				sqlTimingLogger.error(header + " FAILED! " + sql + " {FAILED after " +
					execTime + " msec}", e);
			}
		}
	}

	/**
	 * Called when a JDBC method from a Connection, Statement, PreparedStatement,
	 * CallableStatement or ResultSet returns.
	 * 
	 * @param spy the Spy wrapping the class that called the method that returned.
	 * @param methodCall a description of the name and call parameters of the
	 *        method that returned.
	 * @param returnMsg return value converted to a String for integral types, or
	 *        String representation for Object. Return types this will be null for
	 *        void return types.
	 */
	public void methodReturned(Spy spy, String methodCall, String returnMsg)
	{
		String classType = spy.getClassType();
		Logger logger = ResultSetSpy.classTypeDescription.equals(classType) ? resultSetLogger
			: jdbcLogger;
		if (logger.isInfoEnabled())
		{
			String header = spy.getConnectionNumber() + ". " + classType + "." +
				methodCall + " returned " + returnMsg;
			if (logger.isDebugEnabled())
			{
				logger.debug(header + " " + getDebugInfo());
			}
			else
			{
				logger.info(header);
			}
		}
	}

	/**
	 * Called when a spied upon object is constructed.
	 * 
	 * @param spy the Spy wrapping the class that called the method that returned.
	 * @param constructionInfo information about the object construction
	 */
	public void constructorReturned(Spy spy, String constructionInfo)
	{
		// not used in this implementation -- yet
	}

	private static String nl = System.getProperty("line.separator");

	/**
	 * Determine if the given sql should be logged or not based on the various
	 * DumpSqlXXXXXX flags.
	 * 
	 * @param sql SQL to test.
	 * @return true if the SQL should be logged, false if not.
	 */
	private boolean shouldSqlBeLogged(String sql)
	{
		if (sql == null)
		{
			return false;
		}
		sql = sql.trim();

		if (sql.length() < 6)
		{
			return false;
		}
		sql = sql.substring(0, 6).toLowerCase();
		return (DriverSpy.DumpSqlSelect && "select".equals(sql)) ||
			(DriverSpy.DumpSqlInsert && "insert".equals(sql)) ||
			(DriverSpy.DumpSqlUpdate && "update".equals(sql)) ||
			(DriverSpy.DumpSqlDelete && "delete".equals(sql)) ||
			(DriverSpy.DumpSqlCreate && "create".equals(sql));
	}

	/**
	 * Special call that is called only for JDBC method calls that contain SQL.
	 * 
	 * @param spy the Spy wrapping the class where the SQL occured.
	 * @param methodCall a description of the name and call parameters of the
	 *        method that generated the SQL.
	 * @param sql sql that occured.
	 */
	public void sqlOccured(Spy spy, String methodCall, String sql)
	{
		if (!DriverSpy.DumpSqlFilteringOn || shouldSqlBeLogged(sql))
		{
			if (sqlOnlyLogger.isDebugEnabled())
			{
				sqlOnlyLogger.debug(getDebugInfo() + nl + spy.getConnectionNumber() +
					". " + processSql(sql));
			}
			else if (sqlOnlyLogger.isInfoEnabled())
			{
				sqlOnlyLogger.info(processSql(sql));
			}
		}
	}

	/**
	 * Break an SQL statement up into multiple lines in an attempt to make it more
	 * readable
	 * 
	 * @param sql SQL to break up.
	 * @return SQL broken up into multiple lines
	 */
	private String processSql(String sql)
	{
		if (sql == null)
		{
			return null;
		}

		if (DriverSpy.TrimSql)
		{
			sql = sql.trim();
		}

		StringBuilder output = new StringBuilder();

		if (DriverSpy.DumpSqlMaxLineLength <= 0)
		{
			output.append(sql);
		}
		else
		{
			// insert line breaks into sql to make it more readable
			StringTokenizer st = new StringTokenizer(sql);
			String token;
			int linelength = 0;

			while (st.hasMoreElements())
			{
				token = (String) st.nextElement();

				output.append(token);
				linelength += token.length();
				output.append(" ");
				linelength++;
				if (linelength > DriverSpy.DumpSqlMaxLineLength)
				{
					output.append("\n");
					linelength = 0;
				}
			}
		}

		if (DriverSpy.DumpSqlAddSemicolon)
		{
			output.append(";");
		}

		String stringOutput = output.toString();

		if (DriverSpy.TrimExtraBlankLinesInSql)
		{
			LineNumberReader lineReader = new LineNumberReader(new StringReader(
				stringOutput));

			output = new StringBuilder();

			int contiguousBlankLines = 0;
			try
			{
				while (true)
				{
					String line = lineReader.readLine();
					if (line == null)
					{
						break;
					}

					// is this line blank?
					if (line.trim().length() == 0)
					{
						contiguousBlankLines++;
						// skip contiguous blank lines
						if (contiguousBlankLines > 1)
						{
							continue;
						}
					}
					else
					{
						contiguousBlankLines = 0;
						output.append(line);
					}
					output.append("\n");
				}
			}
			catch (IOException e)
			{
				// since we are reading from a buffer, this isn't likely to happen,
				// but if it does we just ignore it and treat it like its the end of the
				// stream
			}
			stringOutput = output.toString();
		}

		// trim whitespace that is the same from the front of each line in the SQL
		if (DriverSpy.TrimSqlLines)
		{
			// the algorithm below is not the most efficient possible, but it
			// represents a reasonable trade off between performance and
			// maintainability as well as time to develop in the first place.

			// There are a lot of approaches that could be taken to make it run
			// faster if the need arises, but the size of the strings involved
			// shouldn't require that, at least at this time.

			// root line to use for comparison purposes.
			String rootLine = null;

			LineNumberReader lineReader = new LineNumberReader(new StringReader(
				stringOutput));

			// first make one pass to gather the lines into a List
			List<String> linesList = new ArrayList<String>();
			try
			{
				while (true)
				{
					String line = lineReader.readLine();
					if (line == null)
					{
						break;
					}

					line = Utilities.rtrim(line);
					if (rootLine == null && line.length() > 0)
					{
						rootLine = line;
					}

					// any lines that are all whitespace get collapsed here to an empty
					// string so we will know that we can
					// skip those completely in the next stage
					linesList.add(line);
				}
			}
			catch (IOException e)
			{
				// since we are reading from a buffer, this isn't likely to happen,
				// but if it does we just ignore it and treat it like its the end of the
				// stream
			}

			// early termination... only one line, or no output!
			if (rootLine == null || rootLine.length() == 0 || linesList.size() <= 1)
			{
				return stringOutput.trim();
			}

			// now make multiple passes comparing whitespace from each line until
			// a deviation occurs
			// then we know how much whitespace to consume from each line

			int whiteSpaceIndex = -1;

			outer: while (true)
			{
				whiteSpaceIndex++;
				// walk each string until we find non whitespace or divergent types
				// of whitespace

				// we are safe from terminating this loop off the end of the string
				// because the previous step collapsed all white space strings to
				// an empty string and we skip empty strings here, so one of the strings
				// must necessarily terminate the loop via a non-whitespace char
				// (or earlier via diverging types of whitespace , like a space vs. a
				// tab)
				for (String line : linesList)
				{
					// completely blank lines are exempt from this check...
					if (line.equals(""))
					{
						continue;
					}

					Character ch = line.charAt(whiteSpaceIndex);

					// whitespace must match identically to be considered for collapsing
					if (!Character.isWhitespace(ch) ||
						ch != rootLine.charAt(whiteSpaceIndex))
					{
						break outer;
					}
				}
			}

			// now that we know how much we can trim from each string, do the trim
			if (whiteSpaceIndex > 0)
			{
				output = new StringBuilder();
				for (String line : linesList)
				{
					if (!line.equals(""))
					{
						output.append(line.substring(whiteSpaceIndex));
					}
					output.append(nl);
				}
				stringOutput = output.toString();
			}

		}

		return stringOutput;
	}

	/**
	 * Special call that is called only for JDBC method calls that contain SQL.
	 * 
	 * @param spy the Spy wrapping the class where the SQL occurred.
	 * 
	 * @param execTime how long it took the SQL to run, in milliseconds.
	 * 
	 * @param methodCall a description of the name and call parameters of the
	 *        method that generated the SQL.
	 * 
	 * @param sql SQL that occurred.
	 */
	public void sqlTimingOccured(Spy spy, long execTime, String methodCall,
		String sql)
	{
		if (sqlTimingLogger.isErrorEnabled() &&
			(!DriverSpy.DumpSqlFilteringOn || shouldSqlBeLogged(sql)))
		{
			if (DriverSpy.SqlTimingErrorThresholdEnabled &&
				execTime >= DriverSpy.SqlTimingErrorThresholdMsec)
			{
				sqlTimingLogger.error(buildSqlTimingDump(spy, execTime, methodCall,
					sql, sqlTimingLogger.isDebugEnabled()));
			}
			else if (sqlTimingLogger.isWarnEnabled())
			{
				if (DriverSpy.SqlTimingWarnThresholdEnabled &&
					execTime >= DriverSpy.SqlTimingWarnThresholdMsec)
				{
					sqlTimingLogger.warn(buildSqlTimingDump(spy, execTime, methodCall,
						sql, sqlTimingLogger.isDebugEnabled()));
				}
				else if (sqlTimingLogger.isDebugEnabled())
				{
					sqlTimingLogger.debug(buildSqlTimingDump(spy, execTime, methodCall,
						sql, true));
				}
				else if (sqlTimingLogger.isInfoEnabled())
				{
					sqlTimingLogger.info(buildSqlTimingDump(spy, execTime, methodCall,
						sql, false));
				}
			}
		}
	}

	/**
	 * Helper method to quickly build a SQL timing dump output String for logging.
	 * 
	 * @param spy the Spy wrapping the class where the SQL occurred.
	 * 
	 * @param execTime how long it took the SQL to run, in milliseconds.
	 * 
	 * @param methodCall a description of the name and call parameters of the
	 *        method that generated the SQL.
	 * 
	 * @param sql SQL that occurred.
	 * 
	 * @param debugInfo if true, include debug info at the front of the output.
	 * 
	 * @return a SQL timing dump String for logging.
	 */
	private String buildSqlTimingDump(Spy spy, long execTime, String methodCall,
		String sql, boolean debugInfo)
	{
		StringBuffer out = new StringBuffer();

		if (debugInfo)
		{
			out.append(getDebugInfo());
			out.append(nl);
			out.append(spy.getConnectionNumber());
			out.append(". ");
		}

		// NOTE: if both sql dump and sql timing dump are on, the processSql
		// algorithm will run TWICE once at the beginning and once at the end
		// this is not very efficient but usually
		// only one or the other dump should be on and not both.

		sql = processSql(sql);

		out.append(sql);
		out.append(" {executed in ");
		out.append(execTime);
		out.append(" msec}");

		return out.toString();
	}

	/**
	 * Get debugging info - the module and line number that called the logger
	 * version that prints the stack trace information from the point just before
	 * we got it (net.sf.log4jdbc)
	 * 
	 * if the optional log4jdbc.debug.stack.prefix system property is defined then
	 * the last call point from an application is shown in the debug trace output,
	 * instead of the last direct caller into log4jdbc
	 * 
	 * @return debugging info for whoever called into JDBC from within the
	 *         application.
	 */
	private static String getDebugInfo()
	{
		Throwable t = new Throwable();
		t.fillInStackTrace();

		StackTraceElement[] stackTrace = t.getStackTrace();

		if (stackTrace != null)
		{
			String className;

			StringBuffer dump = new StringBuffer();

			/**
			 * The DumpFullDebugStackTrace option is useful in some situations when we
			 * want to see the full stack trace in the debug info- watch out though as
			 * this will make the logs HUGE!
			 */
			if (DriverSpy.DumpFullDebugStackTrace)
			{
				boolean first = true;
				for (int i = 0; i < stackTrace.length; i++)
				{
					className = stackTrace[i].getClassName();
					if (!className.startsWith("net.sf.log4jdbc"))
					{
						if (first)
						{
							first = false;
						}
						else
						{
							dump.append("  ");
						}
						dump.append("at ");
						dump.append(stackTrace[i]);
						dump.append(nl);
					}
				}
			}
			else
			{
				dump.append(" ");
				int firstLog4jdbcCall = 0;
				int lastApplicationCall = 0;

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
				int j = lastApplicationCall;

				if (j == 0) // if app not found, then use whoever was the last guy that
										// called a log4jdbc class.
				{
					j = 1 + firstLog4jdbcCall;
				}

				dump.append(stackTrace[j].getClassName()).append(".")
					.append(stackTrace[j].getMethodName()).append("(")
					.append(stackTrace[j].getFileName()).append(":")
					.append(stackTrace[j].getLineNumber()).append(")");
			}

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

	/**
	 * Called whenever a new connection spy is created.
	 * 
	 * @param spy ConnectionSpy that was created.
	 */
	public void connectionOpened(Spy spy)
	{
		if (connectionLogger.isDebugEnabled())
		{
			connectionLogger.info(spy.getConnectionNumber() + ". Connection opened " +
				getDebugInfo());
			connectionLogger.debug(ConnectionSpy.getOpenConnectionsDump());
		}
		else
		{
			connectionLogger.info(spy.getConnectionNumber() + ". Connection opened");
		}
	}

	/**
	 * Called whenever a connection spy is closed.
	 * 
	 * @param spy ConnectionSpy that was closed.
	 */
	public void connectionClosed(Spy spy)
	{
		if (connectionLogger.isDebugEnabled())
		{
			connectionLogger.info(spy.getConnectionNumber() + ". Connection closed " +
				getDebugInfo());
			connectionLogger.debug(ConnectionSpy.getOpenConnectionsDump());
		}
		else
		{
			connectionLogger.info(spy.getConnectionNumber() + ". Connection closed");
		}
	}
}

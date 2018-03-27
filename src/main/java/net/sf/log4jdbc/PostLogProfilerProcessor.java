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

import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Post processes an existing sqltiming log, and creates a profiling report from it.
 * Name of log file is passed in on the command line as the only argument.
 *
 * Assumptions:
 *
 * 1. Each sql statement in the log is separated by a blank line.
 * 2. Each sql statement is terminated with the timing string "{executed in N nanoSec}" where N is the number of
 *    nanoseconds that the sql executed in.
 *
 */
public class PostLogProfilerProcessor {

  //todo:  needs to be able to gracefully handle sql exceptions in log output

  /**
   * Post Process log4jdbc sqltiming log data.
   *
   * @param args command line arguments.  Expects one argument, the name of the file to post process.
   * @throws Exception if something goes wrong during processing.
   */
  public static void main(String[] args) throws Exception
  {
    if (args.length < 1)
    {
      System.out.println("usage: java PostLogProfilerProcessor <log-file>");
      System.exit(1);
    }
    new PostLogProfilerProcessor(args[0], System.out);
  }

  /**
   * Total number of sql statements processed.
   */
  private long totalSql = 0L;

  /**
   * Number of lines processed.
   */
  private long lineNo = 0L;

  /**
   * Total number of nanoseconds that all processed sql took to run.
   */
  private long totalNanoSec = 0L;

  /**
   * Nanoseconds of the worst single offending sql statement.
   */
  private long maxNanoSec = 0L;

  /**
   * Total combined nanoseconds of all flagged sql statements.
   */
  private long flaggedSqlTotalNanoSec = 0L;

  /**
   * Threshold at which sql is deemed to be running slow enough to be flagged.
   */
  private long threshold = TimeUnit.MILLISECONDS.toNanos(100L);

  /**
   * How many top offender sql statements to display in final report
   */
  private long topOffenderCount = 1000L;

  /**
   * Collection of all sql that took longer than "threshold" nanoseconds to run.
   */
  private List flaggedSql = new LinkedList();

  /**
   * Process given filename, and produce sql profiling report to given PrintStream.
   *
   * @param filename sqltiming log to process.
   * @param out PrintStream to write profiling report to.
   * @throws Exception if reading error occurs.
   */
  public PostLogProfilerProcessor (String filename, PrintStream out) throws Exception
  {
    FileReader f= new FileReader(filename);
    LineNumberReader l = new LineNumberReader(f);

    String line;
    boolean blankLine;

    StringBuffer sql = new StringBuffer();

    do
    {
      line = l.readLine();

      if (line != null)
      {
        blankLine = line.length()==0;
        lineNo++;
/*
        if (lineNo%100000L==0L)
        {
          out.println("" + lineNo + " lines...");
        }
*/
        if (blankLine)
        {
          processSql(sql);
          sql = new StringBuffer();
        }
        else
        {
          sql.append(line);
        }

      }
    } while (line != null);

    out.println("processed " + lineNo + " lines.");

    f.close();

    // display report to stdout

    out.println("Number of sql statements:  " + totalSql);
    out.println("Total number of nanoseconds    :  " + totalNanoSec);
    if (totalNanoSec>0)
    {
      out.println("Average nanosecond/statement  :  " + totalSql/totalNanoSec);
    }

    int flaggedSqlStmts = flaggedSql.size();

    if (flaggedSqlStmts>0)
    {
      out.println("Sql statements that took more than "+ threshold + " nanoseconds were flagged.");
      out.println("Flagged sql statements              :  " + flaggedSqlStmts);
      out.println("Flagged sql Total number of nanoseconds    :  " + flaggedSqlTotalNanoSec);
      out.println("Flagged sql Average nanosecond/statement  :  " + flaggedSqlTotalNanoSec/flaggedSqlStmts);

      out.println("sorting...");

      Object[] flaggedSqlArray = flaggedSql.toArray();
      Arrays.sort(flaggedSqlArray);

      int execTimeSize = ("" + maxNanoSec).length();


      if (topOffenderCount > flaggedSqlArray.length)
      {
        topOffenderCount = flaggedSqlArray.length;
      }

      out.println("top " + topOffenderCount + " offender"+ (topOffenderCount==1?"":"s") + ":");

      ProfiledSql p;

      for (int i=0; i < topOffenderCount; i++)
      {
        p = (ProfiledSql) flaggedSqlArray[i];
        out.println(Utilities.rightJustify(execTimeSize,""+p.getExecTimeNanoSec()) + " " + p.getSql());
      }
    }
  }


  private void processSql(StringBuffer sql)
  {
    if (sql.length()>0)
    {
      totalSql++;
      String sqlStr = sql.toString();
      if (sqlStr.endsWith("nanoSec}"))
      {
        int executedIn = sqlStr.indexOf("{executed in ");
        if (executedIn == -1)
        {
          System.err.println("WARNING:  sql w/o timing info found at line " + lineNo);
          return;
        }

        //todo: proper error handling for parse
        String nanoSecStr = sqlStr.substring(executedIn+13, sqlStr.length()-9);
        long nanoSec = Long.parseLong(nanoSecStr);
        totalNanoSec +=nanoSec;
        if (nanoSec > maxNanoSec)
        {
          maxNanoSec = nanoSec;
        }

        if (nanoSec >threshold)
        {
          flagSql(nanoSec,sqlStr);
          flaggedSqlTotalNanoSec += nanoSec;
        }
      }
      else
      {
        System.err.println("WARNING:  sql w/o timing info found at line " + lineNo);
      }
    }
  }

  private void flagSql(long nanoSec, String sql)
  {
    flaggedSql.add(new ProfiledSql(nanoSec,sql));
  }

  private class ProfiledSql implements Comparable {
    private Long execTimeNanoSec;
    private String sql;

    public ProfiledSql (long nanoSec, String sql)
    {
      this.execTimeNanoSec= new Long(nanoSec);
      this.sql = sql;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     *
     * In this case the comparison is used to sort flagged sql in descending order.
     * @param o ProfiledSql Object to compare to this ProfiledSql.  Must not be null.
     */
    public int compareTo(Object o) {
      return ((ProfiledSql)o).execTimeNanoSec.compareTo(execTimeNanoSec);
    }

    public Long getExecTimeNanoSec() {
      return execTimeNanoSec;
    }

    public String getSql() {
      return sql;
    }

    public String toString()
    {
      return this.execTimeNanoSec + " nanoseconds:  " + this.sql;
    }
  }
}

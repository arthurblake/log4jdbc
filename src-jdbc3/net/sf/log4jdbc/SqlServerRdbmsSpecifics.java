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

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Formatting for SQL Server databases.
 *
 * @author Arthur Blake
 */
class SqlServerRdbmsSpecifics extends RdbmsSpecifics
{
  /**
   * Default constructor.
   */
  SqlServerRdbmsSpecifics()
  {
    super();
  }

  private static final DateFormat sqlServerDateFormat = new SimpleDateFormat("MM/dd/yyyy");
  private static final DateFormat sqlServerTimestampFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");

  /**
   * Format an Object that is being bound to a PreparedStatement parameter, for display. The goal is to reformat the
   * object in a format that can be re-run against the native SQL client of the particular Rdbms being used.  This
   * class should be extended to provide formatting instances that format objects correctly for different RDBMS
   * types.
   *
   * @param object jdbc object to be formatted.
   * @return formatted dump of the object.
   */
  String formatParameterObject(Object object)
  {
    if (object == null)
    {
      return super.formatParameterObject(object);
    }
    if (object instanceof Date)
    {
      return "'" + sqlServerDateFormat.format(object) + "'";
    }
    else if (object instanceof Timestamp)
    {
      return "'" + sqlServerTimestampFormat.format(object) + "'";
    }
    else
    {
      return super.formatParameterObject(object);
    }
  }

}

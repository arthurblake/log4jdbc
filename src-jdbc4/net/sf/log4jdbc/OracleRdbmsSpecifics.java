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

import java.util.Date;

/**
 * RDBMS specifics for the Oracle DB.
 *
 * @author Arthur Blake
 */
class OracleRdbmsSpecifics extends RdbmsSpecifics
{
  OracleRdbmsSpecifics()
  {
    super();
  }
  
  String formatParameterObject(Object object)
  {
    if (object != null && object instanceof Date)
    {
      // Use Oracle's to_date function to insure it comes across as a date
      //
      // for example: 
      //   to_date('12/31/2011 23:59:59.150, 'mm/dd/yyyy hh24:mi:ss.ff3')
      return "to_date('" + dateFormat.format(object) + "', " +
        "'mm/dd/yyyy hh24:mi:ss.ff3')";
    }
    else
    {
      return super.formatParameterObject(object);
    }
  }
}

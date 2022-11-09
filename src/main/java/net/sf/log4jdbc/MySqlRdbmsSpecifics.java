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

import java.text.SimpleDateFormat;

/**
 * RDBMS specifics for the MySql db.
 *
 * @author Arthur Blake
 */
class MySqlRdbmsSpecifics extends RdbmsSpecifics
{
  MySqlRdbmsSpecifics()
  {
    super();
  }

  String formatParameterObject(Object object)
  {
    if (object instanceof java.sql.Time)
    {
      return "'" + new SimpleDateFormat("HH:mm:ss").format(object) + "'";
    }
    else if (object instanceof java.sql.Date)
    {
      return "'" + new SimpleDateFormat("yyyy-MM-dd").format(object) + "'";
    }
    else if (object instanceof java.util.Date)  // (includes java.sql.Timestamp)
    {
      return "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(object) + "'";
    }
    else
    {
      return super.formatParameterObject(object);
    }
  }
}
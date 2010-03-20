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

/**
 * Common interface that all Spy classes can implement.
 * This is used so that any class that is being spied upon can transmit generic information about
 * itself to the whoever is doing the spying.
 *
 * @author Arthur Blake
 */
public interface Spy
{

  /**
   * Get the type of class being spied upon.  For example, "Statement", "ResultSet", etc.
   *
   * @return a description of the type of class being spied upon.
   */
  public String getClassType();

  /**
   * Get the connection number.  In general, this is used to track which underlying connection is being
   * used from the database.  The number will be incremented each time a new Connection is retrieved from the
   * real underlying jdbc driver.  This is useful for debugging and tracking down problems with connection pooling.
   *
   * @return the connection instance number.
   */
  public Integer getConnectionNumber();
}

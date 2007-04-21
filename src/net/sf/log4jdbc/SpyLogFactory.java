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

/**
 * A provider for a SpyLogDelegator.  This allows a single switch point to abstract
 * away which logging system to use for spying on JDBC calls.
 *
 * At this point, only the log4j system can be used, but it would be trivial to extend the system
 * for use with any other logging system, by creating new implementations of SpyLogDelegator.
 *
 * @author Arthur Blake
 */
public class SpyLogFactory
{
  /**
   * Do not allow instantiation.  Access is through static method.
   */
  private SpyLogFactory() {}

  /**
   * The logging system of choice.
   */
  private static final SpyLogDelegator logger = new Log4jSpyLogDelegator();

  /**
   * Get the default SpyLogDelegator for logging to the logger.
   *
   * @return the default SpyLogDelegator for logging to the logger.
   */
  public static SpyLogDelegator getSpyLogDelegator()
  {
    return logger;
  }
}

